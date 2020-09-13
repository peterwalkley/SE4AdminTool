package tfa.se4;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

// Jetty documentation:  https://www.eclipse.org/jetty/documentation/current/websocket-jetty.html
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import tfa.se4.PlayerBanList.Ban;
import tfa.se4.Protocol.ReplyMessage;
import tfa.se4.json.JSONUtils;
import tfa.se4.json.Player;
import tfa.se4.json.ServerStatus;
import tfa.se4.logger.LoggerInterface;
import tfa.se4.steam.SteamAPI;

/**
 * Main handler class for a connection to SE4 server.
 */
@WebSocket
public class SEAdminServerConnection implements LoggerInterface, Runnable
{
	private String m_host;
	private String m_port;
	private String m_password;
    private final CountDownLatch m_closeLatch;
    private Session m_session;
    private ServerStatus m_serverStatus;
    private IPBanList m_ipBans;
    private PlayerWhiteList m_whiteList;
    private PlayerBanList m_banList;
    private String m_playerGreeting;
    private WebSocketClient m_client = null;
    private boolean m_isApplyVACBans;
    private boolean m_isApplyGameBans;
    private SteamAPI m_steamAPI;
    private ConcurrentLinkedQueue<Player> m_playersToCheck = new ConcurrentLinkedQueue<>();
    private LogLevel m_displayLogLevel = LogLevel.INFO;
    private boolean m_isClose = false;
   
    /**
     * Set up and manage connection based on properties configuration.
     * @param options Configuration options.
     */
    public SEAdminServerConnection(final Options options)
    {
		m_host = options.getHost();
		m_port = options.getPort();
		m_password = options.getPassword();
        m_closeLatch = new CountDownLatch(1);
		m_ipBans = new IPBanList(options.getIPBansFile(), this);
		m_whiteList = new PlayerWhiteList(options.getWhiteListFile(), this);
		m_banList = new PlayerBanList(options.getPlayerBansListFile(), this);
		m_playerGreeting = options.getPlayerGreeting();
		m_isApplyVACBans = options.isApplyVACBans();
		m_isApplyGameBans = options.isApplyGameBans();
		if (m_isApplyVACBans || m_isApplyGameBans)
		{
		    m_steamAPI = new SteamAPI(options.getSteamAPIKey());
		}
        new Thread(this).start();
    }

    /**
     * Management thread. Polls for dead connection and tries
     * reconnect after 10 seconds.
     */
    @Override
    public void run()
    {
        while (!m_isClose)
        {
            if (m_client == null)
            {
                try
                {
                    final WebSocketClient client = new WebSocketClient();
                    client.start();
                    Future<Session> future = client.connect(this, this.getURI());
                    future.get(10, TimeUnit.SECONDS);
                    m_client = client;
                }
                catch (final Exception ex)
                {
                    log(LogLevel.INFO, LogType.SYSTEM, "Connect failed. Retrying in 10 seconds.");
                    log(LogLevel.TRACE, LogType.SYSTEM, ex, "Connection error");
                    sleep(10000);
                }
            }
            else
            {
                while (!m_playersToCheck.isEmpty())
                {
                    checkPlayer(m_playersToCheck.remove());
                }
                sleep(1000);
            }
        }

        try
        {
            if (m_client != null)
                m_client.stop();

            if (m_session != null) {
                m_session.disconnect();
                m_session.close();
            }
        } catch (Exception e) {
            log(LogLevel.TRACE, LogType.SYSTEM, e, "Exception during shutdown");
            // ignore
        }
        finally {
            m_client = null;
            m_session = null;
        }
    }

    /**
     * Close connection as tidily as we can.
     */
    public synchronized void closeConnection()
    {
        log(LogLevel.INFO, LogType.SYSTEM, "Closing connection to %s:%s", m_host, m_port);
        m_isClose = true;
    }
    /**
     * Check whether a player is allowed here.
     * @param p Player to check.
     */
    private void checkPlayer(final Player p)
    {
        if (!m_serverStatus.getLobby().getPlayers().contains(p))
        {
            log(LogLevel.INFO, LogType.CHECK, "Player %s steam ID %s from IP address %s left before checks performed", p.getName(), p.getSteamId(), p.getIPv4());
            return;
        }
        
        if (m_whiteList.isWhitelisted(p.getSteamId()))
        {
            log(LogLevel.INFO, LogType.WHITELIST, "Player %s steam ID %s joined from IP address %s", p.getName(), p.getSteamId(), p.getIPv4());
            doGreeting(p);
            return;
        }
        
        if (m_ipBans.isBanned(p.getIPv4()))
        {
            log(LogLevel.INFO, LogType.IPBAN,"Player %s steam ID %s joined from banned IP address %s", p.getName(), p.getSteamId(), p.getIPv4());
            banPlayer(p, "banned IP address");
            return;
        }
        
        if (m_banList.getBan(p.getSteamId()) != null)
        {
            final Ban banInfo = m_banList.getBan(p.getSteamId());
            log(LogLevel.INFO, LogType.PLAYERBAN,"Banned player %s steam ID %s for reason '%s' joined from banned IP address %s. Name at ban was %s", p.getName(), p.getSteamId(), banInfo.reason, p.getIPv4(), banInfo.name);
            banPlayer(p, banInfo.reason);
            return;
        }
        
        if (m_isApplyVACBans || m_isApplyGameBans)
        {
            final tfa.se4.steam.json.Player banInfo = m_steamAPI.getBanInfo(p.getSteamId(), p.getName(), this);
            if (banInfo != null)
            {
                if (m_isApplyVACBans && banInfo.getVACBanned())
                {
                    log(LogLevel.INFO, LogType.VAC, "Player %s steam ID %s has VAC ban", p.getName(), p.getSteamId());
                    banPlayer(p, "VAC ban");
                    return;
                }
                
                if (m_isApplyGameBans && banInfo.getNumberOfGameBans() > 0)
                {
                    log(LogLevel.INFO, LogType.GAMEBAN, "Player %s steam ID %s has game ban", p.getName(), p.getSteamId());
                    banPlayer(p, "Game ban");
                    return;
                }
            }
        }
        
        // Passed all kick checks.
        doGreeting(p);
    }

    /**
     * Kick player.
     * @param p Player to ban
     * @param reason Reason
     */
    public void kickPlayer(final Player p, final String reason)
    {
        new Thread(new Runnable() {

            @Override
            public void run() {
                // Make the ban public
                final String msg = "Server.Say KICKING " + p.getName() + " for " + reason;
                sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());

                // Delay 5 seconds
                sleep(5000);

                // Kick them
                final String ban = "Server.Kick " + p.getName();
                sendMessage(Protocol.REQUEST_SEND_COMMAND, ban.getBytes());
            }

        }).start();
    }

    /**
     * Apply player ban.
     * @param p Player to ban
     * @param reason Reason
     */
    public void banPlayer(final Player p, final String reason)
    {
        new Thread(new Runnable() {

            @Override
            public void run() {
                m_banList.addBan(p.getSteamId(), p.getName(), reason);
                // Do a steam ID ban to make sure they are added to server ban list
                final String steamIDBan = "Server.KickBanSteamID " + p.getSteamId();
                sendMessage(Protocol.REQUEST_SEND_COMMAND, steamIDBan.getBytes());

                // Make the ban public
                final String msg = "Server.Say BANNING " + p.getName() + " for " + reason;
                sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());

                // Delay 5 seconds
                sleep(5000);

                // Kick them
                final String ban = "Server.KickBan " + p.getName();
                sendMessage(Protocol.REQUEST_SEND_COMMAND, ban.getBytes());
            }

        }).start();
    }

    /**
     * Send server a command
     * @param command Reason
     */
    public void sendCommand(final String command)
    {
        new Thread(new Runnable() {

            @Override
            public void run() {
                sendMessage(Protocol.REQUEST_SEND_COMMAND, command.getBytes());
            }

        }).start();
    }

    /**
     * Get URL.
     * @return ws://<<host>:<port>
     * @throws URISyntaxException Shouldn't happen unless bad input
     */
    public URI getURI() throws URISyntaxException
    {
    	return new URI("ws://" + m_host + ":" + m_port);    	
    }
    
    /**
     * Update state based on receiving a {@link Protocol#REPLY_WEB_STATUS_UPDATE} notification.
     * The payload is JSON which we parse to a {@link tfa.se4.json.ServerStatus} structure.
     * <ul>
     * <li>Check for players joining</li>
     * <li>Check for players leaving</li>
     * <li>Check for change in game state</li>
     * </ul>
     * @param content Payload content from incoming message.
     */
    private synchronized void setServerStatus(final ByteBuffer content)
    {
    	ServerStatus status = JSONUtils.unMarshalServerStatus(Protocol.payloadAsUTF8String(content), this);
    	if (status == null) // do nothing when we've got bad JSON
    		return;
    	
    	final List<Player> newPlayers = status.getLobby().getPlayers();
    	final List<Player> oldPlayers = m_serverStatus == null ? new ArrayList<>() : m_serverStatus.getLobby().getPlayers();
    	
    	for (final Player p : newPlayers)
    	{
    		if (!oldPlayers.contains(p))
    		{
    		    if (m_serverStatus == null) // signal to skip the greeting if we're just starting up the monitoring
    		        p.setSkipGreeting(true);
    		    
    		    m_playersToCheck.add(p);
                log(LogLevel.INFO, LogType.JOIN, "Player %s steam ID %s joined from IP address %s",p.getName(),p.getSteamId(),  p.getIPv4());
    		}
    	}
    	
    	for (final Player p : oldPlayers)
    	{
    		if (!newPlayers.contains(p))
    		{
                log(LogLevel.INFO, LogType.LEAVE, "Player %s steam ID %s left from IP address %s",p.getName(),p.getSteamId(),  p.getIPv4());
    		}
    	}
    	
    	final String newGameState = status.getLobby().getState();
    	final String oldGameState = m_serverStatus == null ? "unknown" : m_serverStatus.getLobby().getState();
    	if (!oldGameState.equals(newGameState))
    	{
			if (Protocol.IN_GAME.equals(oldGameState))
			    log(LogLevel.INFO, LogType.GAME_ENDED, JSONUtils.marshalServerStatus(m_serverStatus, this));
			else
                log(LogLevel.DEBUG, LogType.GAME_DATA, Protocol.payloadAsUTF8String(content));
    	}

    	setServerStatus(status);
    }

    /**
     * Status update handler for inherited classes to over-ride.
     * @param status Server status from SE
     */
    public void setServerStatus(final ServerStatus status)
    {
        m_serverStatus = status;
    }

    /**
     * Send a player greeting message (if configured)
     * @param p Player to greet.
     */
    private void doGreeting(final Player p)
    {
        // Don't greet for when we've just started up or there's no greeting message
        if (StringUtils.isNotBlank(m_playerGreeting) && !p.isSkipGreeting())
        {
            final String msg = "Server.Say "+ m_playerGreeting.
                    replace("#PlayerName#", p.getName()).
                    replace("#ServerName#", m_serverStatus.getServer().getName());
            sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());
            sleep(1000);
        }
    }
    
    /**
     * Simple latch so caller will wait until this is closed.
     * @throws InterruptedException Never :)
     */
    public void awaitClose() throws InterruptedException
    {
        m_closeLatch.await();
    }

    /**
     * Send message to SE4 server.
     * @param messageId Message ID from {@link Protocol}
     */
    private void sendMessage(final char messageId)
    {
        final ByteBuffer buf = Protocol.buildMessage(messageId);
        try
        {
            log(LogLevel.TRACE, LogType.SYSTEM, ">> %s", Protocol.bytesToString(buf.array()));
        	m_session.getRemote().sendBytes(buf);
        }
        catch (final IOException ex)
        {
        	ex.printStackTrace();
        }
    }
    
    /**
     * Send message with payload to SE4 server.
     * @param messageId Message ID from {@link Protocol}.
     * @param msg Message payload.
     */
    private void sendMessage(final char messageId, final byte[] msg)
    {
    	final ByteBuffer buf = Protocol.buildMessage(messageId, msg);
        
        try
        {
            log(LogLevel.TRACE, LogType.SYSTEM, ">> %s", Protocol.bytesToString(buf.array()));
        	m_session.getRemote().sendBytes(buf);
        }
        catch (final IOException ex)
        {
        	ex.printStackTrace();
        }
    }
    
    /**
     * Handle income message after we've split it into ID and (optional) payload.
     * @param messageId MessageId
     * @param content Payload - may be empty
     */
    private void handleMessage(final char messageId, final ByteBuffer content)
    {
    	switch (messageId)
    	{
    	case Protocol.REPLY_INIT :
    	    log(LogLevel.INFO, LogType.SYSTEM, "Authenticating");
    		final byte[] salt = Protocol.getSaltValue(content);
    		sendMessage(Protocol.REQUEST_SEND_PWD, Protocol.buildSaltedPassword(salt, m_password));
    		break;
    		
    	case Protocol.REPLY_CONNECTION_SUCCESS_1 : 
    	case Protocol.REPLY_CONNECTION_SUCCESS_2 : 
            log(LogLevel.INFO, LogType.SYSTEM, "Authentication successful");
    		sendMessage(Protocol.REQUEST_SET_WEB_STATUS_UPDATE_INTERVAL, Protocol.uInt32ToBytes(5000));
    		break;
    		
    	case Protocol.REPLY_WEB_STATUS_UPDATE :
    		setServerStatus(content);
    		break;
    		
    	case Protocol.REPLY_ASYNC_MSG :
    		final String message = Protocol.payloadAsUTF8String(content);
            log(LogLevel.INFO, LogType.MSG, message);
    		break;
    		
    	case Protocol.REPLY_WEB_STATUS_MONITOR_UPDATE :
    		handleWebStatusMonitorUpdate(content);
    		break;
    		
    	case Protocol.REPLY_COMMAND_LIST: // triggered as a result of REQUEST_SET_WEB_STATUS_UPDATE_INTERVAL being sent
    		final String cmdList = Protocol.payloadAsUTF8String(content);
            log(LogLevel.DEBUG, LogType.MSG, "command list %s", cmdList);
    		break;
    		
    	case Protocol.REPLY_CMD_RESULT:
            log(LogLevel.DEBUG, LogType.SYSTEM, content.get() == 0 ? "FAIL" : "OK");
        	break;
        	
    	default:
            log(LogLevel.INFO, LogType.SYSTEM, "Unhandled message id = %s payload = %s", "" + messageId, Protocol.bytesToString(content.array()));
    	}
    }
    
    /**
     * Handle web status monitor message. It gives us updates on traffic in and out
     * and FPS.
     * @param content Message payload.
     */
    private synchronized void handleWebStatusMonitorUpdate(final ByteBuffer content)
    {
    	if (content.array().length == 12)
    	{
            updateServerStatistics(Protocol.getUInt32(content, 0),Protocol.getUInt32(content, 4), Protocol.getFloat(content, 8));
    	}
    	else
    	{
    	    log(LogLevel.ERROR, LogType.SYSTEM, "Unexpected content length for WebStatusMonitorUpdate %s", Protocol.bytesToString(content.array()));
    	}
    }

    /**
     * Update server statistics method to allow inherited classes to override.
     * @param bytesSent Bytes send
     * @param bytesReceived Bytes received
     * @param fps FPS
     */
    public void updateServerStatistics(final long bytesSent, final long bytesReceived, final float fps)
    {
        // Don't care.
    }

    /**
     * Handler for when we receive an open web socket event. We trigger the
     * password exchange handshake here.
     * @param session Web socket session.
     */
    @OnWebSocketConnect
    public void onConnect(Session session)
    {
        log(LogLevel.INFO, LogType.SYSTEM, "Connected to %s", session.getRemoteAddress().toString());
        m_session = session;
        if (m_session.isOpen())
            sendMessage(Protocol.REQUEST_INIT);
    }

    /**
     * Handler for when we receive a close web socket event. Just close the connection.
     * @param statusCode Status code returned.
     * @param reason Reason for closure.
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason)
    {
        log(LogLevel.INFO, LogType.SYSTEM,"Connection closed. Status code is '%s' reason is '%s", Integer.toString(statusCode), reason);
        m_session = null;
        m_client = null;
    }

    /**
     * Handler for incoming messages from SE4 server. We split the message
     * into its ID and payload then hand off to {@link #handleMessage(char, ByteBuffer)}
     * to deal with it.
     *  
     * @param session Web socket session
     * @param b Message bytes
     * @param offset Offset into bytes, always zero
     * @param length Length of message, always same as b.length
     */
    @OnWebSocketMessage
    public void onMessage(Session session, byte[] b, int offset, int length)
    {
        log(LogLevel.TRACE, LogType.SYSTEM, "<<||%s offset=%s length=%s", Protocol.bytesToString(b), Integer.toString(offset), Integer.toString(length));
        if (offset != 0 || length != b.length)
        {
            // Never seems to happen from SE,but just in case.
            return;
        }
        
        final ReplyMessage msg = Protocol.getReplyMessage(b);
        handleMessage(msg.messageId, msg.payload);
    }

    /**
     * Handler for any error signalled. In general all we can do is assume
     * they mean the connection is dead.
     * @param cause Underlying cause.
     */
    @OnWebSocketError
    public void onError(Throwable cause)
    {
        if (cause instanceof java.net.ConnectException ||           // Remote connection died or server was killed.
            cause instanceof java.net.SocketTimeoutException ||     // timeout connecting
            cause instanceof java.io.IOException                    // Broken pipe
            )
        {
            log(LogLevel.INFO, LogType.SYSTEM, "WebSocket error handled as connection death");
            m_session = null;
            m_client = null;
        }
        else
        {
            log(LogLevel.ERROR, LogType.SYSTEM, cause,"Unhandled webSocket Error");
        }
    }
    
    /**
     * Force current thread to sleep a bit.
     * @param millis Milliseconds.
     */
    private static void sleep(final int millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (final Exception ex2)
        {
            // ignore
        }
    }
    @Override
    public void log(LogLevel level, LogType type, String message, Object... args) {

        log(level, type, null, message, args);
    }

    @Override
    public void log(LogLevel level, LogType type, Throwable t, String message, Object... args) {

        if (isFilterMessage(level))
            return;

        final StringBuilder sb = new StringBuilder(128);
        sb.append(Instant.now().toString());
        sb.append('|');
        sb.append(level.label);
        sb.append('|');
        sb.append(type.label);
        sb.append('|');
        sb.append(String.format(message, args));

        if (t != null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            sb.append('\n');
            sb.append(sw.toString());
        }

        System.out.println(sb.toString());
    }

    /**
     * Whether to filter out messages of the specified log level.
     * @param level Log level to check
     * @return true if NOT to display it.
     */
    protected boolean isFilterMessage(final LogLevel level)
    {
        if (level.equals(LogLevel.ERROR))
            return false; // always show ERROR

        switch(m_displayLogLevel) {
            case INFO: return level.equals(LogLevel.DEBUG) || level.equals(LogLevel.TRACE);
            case DEBUG: return level.equals(LogLevel.TRACE);
            case TRACE:
            default:
                return false;
        }
    }
    public static void main(String[] args) throws Exception
	{
		if (args.length != 1)
		{
			System.out.println("Usage:\njava tfa.se4.SEAdminServerConnection <config file>");
			System.exit(0);
		}

		final SEAdminServerConnection socket = new SEAdminServerConnection(new Options(args[0]));
        try
        {
            // wait for closed socket connection.
            socket.awaitClose();
        }
        finally
        {
            // nothing
        }
    }

}
