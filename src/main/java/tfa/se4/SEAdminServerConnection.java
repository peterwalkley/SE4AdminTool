package tfa.se4;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import tfa.se4.steam.SteamAPI;

/**
 * Main handler class for a connection to SE4 server.
 */
@WebSocket
public class SEAdminServerConnection implements Runnable
{
	private String m_host;
	private String m_port;
	private String m_password;
    private final CountDownLatch m_closeLatch;
    private Session m_session;
    private long m_bytesReceived;
    private long m_bytesSent;
    private float m_fps;
    private ServerStatus m_serverStatus;
    private Logger m_logger;
    private IPBanList m_ipBans;
    private PlayerWhiteList m_whiteList;
    private PlayerBanList m_banList;
    private String m_playerGreeting;
    private WebSocketClient m_client = null;
    private boolean m_isApplyVACBans;
    private boolean m_isApplyGameBans;
    private SteamAPI m_steamAPI;
   
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
        m_logger = LoggerFactory.getLogger(m_host + ":" + m_port);
		m_ipBans = new IPBanList(options.getIPBansFile(), m_logger);
		m_whiteList = new PlayerWhiteList(options.getWhiteListFile(), m_logger);
		m_banList = new PlayerBanList(options.getPlayerBansListFile(), m_logger);
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
        while (true)
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
                    m_logger.info("Connect failed. Retrying in 10 seconds.");
                    m_logger.trace("Connection error", ex);
                    sleep(10000);
                }
            }
            else
            {
                sleep(1000);
            }
        }
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
     * Server statistic updated periodically.
     * @return bytes received.
     */
    public synchronized long getBytesReceived()
    {
    	return m_bytesReceived;
    }
    /**
     * Server statistic updated periodically.
     * @return bytes sent.
     */
    public synchronized long getBytesSent()
    {
    	return m_bytesSent;
    }
    
    /**
     * Server statistic updated periodically.
     * @return FPS.
     */
    public synchronized float getFPS()
    {
    	return m_fps;
    }
    
    /**
     * Expose server state to outside world. This will
     * be updated roughly every 5 seconds.
     * @return
     */
    public synchronized ServerStatus getServerStatus()
    {
    	return m_serverStatus;
    }
    
    /**
     * Update state based on receiving a {@link Protocol.REPLY_WEB_STATUS_UPDATE} notification.
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
    	ServerStatus status = JSONUtils.unMarshalServerStatus(Protocol.payloadAsUTF8String(content), m_logger);
    	if (status == null) // do nothing when we've got bad JSON
    		return;
    	
    	final List<Player> newPlayers = status.getLobby().getPlayers();
    	final List<Player> oldPlayers = m_serverStatus == null ? new ArrayList<Player>() : m_serverStatus.getLobby().getPlayers();
    	
    	for (final Player p : newPlayers)
    	{
    		if (!oldPlayers.contains(p))
    		{
                if (m_whiteList.isWhitelisted(p.getSteamId()))
                {
                    m_logger.info("WHITELIST|{}|{} joined from IP address {}" , p.getSteamId(), p.getName(), p.getIPv4());
                    doGreeting(status, p);
                    continue;
                }
                
    			if (m_ipBans.isBanned(p.getIPv4()))
    			{
                    m_logger.info("IPBAN|{}|{} joined from banned IP address {}" , p.getSteamId(), p.getName(), p.getIPv4());
        			final String msg = "Server.Say BANNING " + p.getName() + " for banned IP address";
        			sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());
        			final String ban = "Server.KickBanSteamID " + p.getSteamId();
        			sendMessage(Protocol.REQUEST_SEND_COMMAND, ban.getBytes());
        			continue;
    			}
    			else if (m_banList.getBan(p.getSteamId()) != null)
    			{
                    final Ban banInfo = m_banList.getBan(p.getSteamId());
                    m_logger.info("PLAYERBAN|{}|{} for {}. Name when banned was {}" , p.getSteamId(), p.getName(), banInfo.reason, banInfo.name);
    				
        			final String msg = "Server.Say BANNING " + p.getName() + " for " + banInfo.reason;
        			sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());
        			final String ban = "Server.KickBanSteamID " + p.getSteamId();
        			sendMessage(Protocol.REQUEST_SEND_COMMAND, ban.getBytes());
                    continue;
    			}
                else if (m_isApplyVACBans || m_isApplyGameBans)
                {
                    final tfa.se4.steam.json.Player banInfo = m_steamAPI.getBanInfo(p.getId(), m_logger);
                    
                    if (banInfo != null)
                    {
                        if (banInfo.getVACBanned())
                        {
                            m_logger.info("VACBAN|{}|{} for {}." , p.getSteamId(), p.getName(), "VAC");
                            final String msg = "Server.Say BANNING " + p.getName() + " for VAC Ban";
                            sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());
                            final String ban = "Server.KickBanSteamID " + p.getSteamId();
                            sendMessage(Protocol.REQUEST_SEND_COMMAND, ban.getBytes());
                            continue;
                        }
                        
                        if (banInfo.getNumberOfGameBans() > 0)
                        {
                            m_logger.info("GAMEBAN|{}|{} for {}." , p.getSteamId(), p.getName(), "Game Ban");
                            final String msg = "Server.Say BANNING " + p.getName() + " for Game Ban";
                            sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());
                            final String ban = "Server.KickBanSteamID " + p.getSteamId();
                            sendMessage(Protocol.REQUEST_SEND_COMMAND, ban.getBytes());
                            continue;
                        }
                    }
                }
    			
                m_logger.info("JOIN|{}|{} joined from IP address {}" , p.getSteamId(), p.getName(), p.getIPv4());
			    doGreeting(status, p);
    		}
    	}
    	
    	for (final Player p : oldPlayers)
    	{
    		if (!newPlayers.contains(p))
    		{
    			m_logger.info("LEAVE|{}|{} left from IP address {}" , p.getSteamId(), p.getName(), p.getIPv4());
    		}
    	}
    	
    	final String newGameState = status.getLobby().getState();
    	final String oldGameState = m_serverStatus == null ? "unknown" : m_serverStatus.getLobby().getState();
    	if (!oldGameState.equals(newGameState))
    	{
			m_logger.info("GAME||Status {}", newGameState);
			if (Protocol.IN_GAME.equals(oldGameState))
				m_logger.info("GAME_ENDED||{}", JSONUtils.marshalServerStatus(m_serverStatus, m_logger));
			else
				m_logger.info("GAME||{}", Protocol.payloadAsUTF8String(content));
    	}
    	m_serverStatus = status;
    }
    
    /**
     * Send a player greeting message (if configured)
     * @param status Game state
     * @param p Player to greet.
     */
    private void doGreeting(final ServerStatus status, final Player p)
    {
        // Don't greet for when we've just started up or there's no greeting message
        if (m_serverStatus != null && StringUtils.isNotBlank(m_playerGreeting))
        {
            final String msg = "Server.Say "+ m_playerGreeting.
                    replace("#PlayerName#", p.getName()).
                    replace("#ServerName#", status.getServer().getName());
            sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());
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
        	m_logger.debug(">>||{}", Protocol.bytesToString(buf.array()));
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
        	m_logger.trace(">>||{}", Protocol.bytesToString(buf.array()));
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
        	m_logger.info("AUTH||Authenticating");
    		final byte[] salt = Protocol.getSaltValue(content);
    		sendMessage(Protocol.REQUEST_SEND_PWD, Protocol.buildSaltedPassword(salt, m_password));
    		break;
    		
    	case Protocol.REPLY_CONNECTION_SUCCESS_1 : 
    	case Protocol.REPLY_CONNECTION_SUCCESS_2 : 
        	m_logger.info("AUTH||Authentication successful");
    		sendMessage(Protocol.REQUEST_SET_WEB_STATUS_UPDATE_INTERVAL, Protocol.uInt32ToBytes(5000));
    		break;
    		
    	case Protocol.REPLY_WEB_STATUS_UPDATE :
    		setServerStatus(content);
    		break;
    		
    	case Protocol.REPLY_ASYNC_MSG :
    		final String message = Protocol.payloadAsUTF8String(content);
        	m_logger.info("MSG||{}", message);
    		break;
    		
    	case Protocol.REPLY_WEB_STATUS_MONITOR_UPDATE :
    		handleWebStatusMonitorUpdate(content);
    		break;
    		
    	case Protocol.REPLY_COMMAND_LIST: // triggered as a result of REQUEST_SET_WEB_STATUS_UPDATE_INTERVAL being sent
    		final String cmdList = Protocol.payloadAsUTF8String(content);
        	m_logger.info("MSG||Command list {}", cmdList);
    		break;
    		
    	case Protocol.REPLY_CMD_RESULT:
        	m_logger.info("COMMAND||{}", content.get() == 0 ? "FAIL" : "OK");
        	break;
        	
    	default:
        	m_logger.info("MSG||id {} data {}", (int)messageId, Protocol.bytesToString(content.array()));
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
			m_bytesSent = Protocol.getUInt32(content, 0);
			m_bytesReceived = Protocol.getUInt32(content, 4);
			m_fps = Protocol.getFloat(content, 8);
    	}
    	else
    	{
        	m_logger.debug("MSG||Unexpected content length for WebStatusMonitorUpdate {}", Protocol.bytesToString(content.array()));
    	}
    }
    
    /**
     * Handler for when we receive an open web socket event. We trigger the
     * password exchange handshake here.
     * @param session Web socket session.
     */
    @OnWebSocketConnect
    public void onConnect(Session session)
    {
        m_logger.info("CONNECT||Connected to {}", session.getRemoteAddress());
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
        m_logger.info("CLOSE||Status code {} reason {}", statusCode, reason);
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
        m_logger.trace("<<||{} offset={} length={}", Protocol.bytesToString(b), offset, length);
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
            m_logger.debug("WebSocket error handled as connection death: ", cause);
            m_session = null;
            m_client = null;
        }
        else
        {
            m_logger.info("Unhandled webSocket Error: ", cause);
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
        }
    }
}
