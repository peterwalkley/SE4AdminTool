package tfa.se4;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;

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
import tfa.se4.iplookup.IPInformation;
import tfa.se4.iplookup.IPLookupInterface;
import tfa.se4.iplookup.extremeip.ExtremeIPAPI;
import tfa.se4.json.JSONUtils;
import tfa.se4.json.Player;
import tfa.se4.json.ServerStatus;
import tfa.se4.logger.LoggerInterface;
import tfa.se4.steam.SteamAPI;
import tfa.se4.steam.json.PlayerSummaryInfo;

/**
 * Main handler class for a connection to SE4 server.
 */
@WebSocket
public class SEAdminServerConnection implements LoggerInterface, Runnable
{
    private final CountDownLatch mCloseLatch;
    private Session mSession;
    private ServerStatus mServerStatus;
    private final IPBanList mIpBans;
    private final PlayerWhiteList mWhiteList;
    private final PlayerBanList mBanList;
    private WebSocketClient mClient = null;
    private SteamAPI mSteamAPI;
    private IPLookupInterface mIpLookup;
    private final ConcurrentLinkedQueue<Player> mPlayersToCheck = new ConcurrentLinkedQueue<>();
    private static final LogLevel mDisplayLogLevel = LogLevel.INFO;
    private boolean mIsClose = false;
    private final Options mOptions;
    private long mGameStartTime = 0;
    private int mRetryTimeoutSecs = 10;
    private final Map<String,Long> mPlayerJoinTime = new HashMap<>();

    /**
     * Set up and manage connection based on property configuration.
     *
     * @param options Configuration options.
     */
    public SEAdminServerConnection(final Options options)
    {
        mOptions = options;
        mCloseLatch = new CountDownLatch(1);
        mIpBans = new IPBanList(options.getIPBansFile(), this);
        mWhiteList = new PlayerWhiteList(options.getWhiteListFile(), this);
        mBanList = new PlayerBanList(options.getPlayerBansListFile(), this);
        if (StringUtils.isNotBlank(options.getSteamAPIKey()))
        {
            mSteamAPI = new SteamAPI(options.getSteamAPIKey());
        }

        if (StringUtils.isNotBlank(options.getExtremeIPAPIKey()))
        {
            this.log(LogLevel.INFO, LogType.IPINFO, "Initialising Extreme IP");
            mIpLookup = new ExtremeIPAPI(options.getExtremeIPAPIKey());
        }
        new Thread(this).start();
    }

    /**
     * Management thread. Polls for dead connection and try to
     * reconnect after 10 seconds, then increase timeout to a max
     */
    @Override
    public void run()
    {
        while (!mIsClose)
        {
            if (mClient == null)
            {
                try
                {
                    final WebSocketClient client = new WebSocketClient();
                    client.start();
                    Future<Session> future = client.connect(this, this.getURI());
                    future.get(5000, TimeUnit.SECONDS);
                    mClient = client;
                }
                catch (final Exception ex) //NOSONAR ignore whinge about InterruptedException
                {
                    log(LogLevel.INFO, LogType.SYSTEM, "Connect failed. Retrying in " + mRetryTimeoutSecs + " seconds.");
                    log(LogLevel.TRACE, LogType.SYSTEM, ex, "Connection error");
                    Utils.sleep(mRetryTimeoutSecs * 1000L);
                    mRetryTimeoutSecs = getNextRetryTimeSecs(mRetryTimeoutSecs);
                }
            }
            else
            {
                if (!mPlayersToCheck.isEmpty())
                {
                    checkPlayer(mPlayersToCheck.remove());
                }
            }
            Utils.sleep(1000);
        }

        try
        {
            if (mClient != null)
            {
                mClient.stop();
            }

            if (mSession != null)
            {
                mSession.disconnect();
                mSession.close();
            }
        }
        catch (Exception e)
        {
            log(LogLevel.TRACE, LogType.SYSTEM, e, "Exception during shutdown");
            // ignore
        }
        finally
        {
            mClient = null;
            mSession = null;
        }
    }

    /**
     * Do a gradual increase of connect retry time up to 10 minutes between retries.
     * @param currentTime Current retry time
     * @return New retry time
     */
    private int getNextRetryTimeSecs(final int currentTime)
    {
        if (currentTime <= 10)
        {
            return 20;
        }
        else if (currentTime <= 20)
        {
            return 30;
        }
        else if (currentTime <= 30)
        {
            return 40;
        }
        else if (currentTime <= 40)
        {
            return 60;
        }
        else if (currentTime <= 60)
        {
            return 90;
        }
        else if (currentTime <= 90)
        {
            return 180;
        }
        else if (currentTime <= 180)
        {
            return 300;
        }
        else
        {
            return 600;
        }
    }

    /**
     * Provide inherited classes access to the IP Stack API
     *
     * @return IP stack API (may be null)
     */
    protected IPLookupInterface getIPLookup()
    {
        return mIpLookup;
    }

    /**
     * Provide inherited classes access to the steam API
     *
     * @return Steam API (can be null)
     */
    protected SteamAPI getSteamAPI()
    {
        return mSteamAPI;
    }

    /**
     * Provided inherited classes access to the game start time
     * @return game start time. Value will be zero if we are not
     * in-game or don't know when the match started.
     */
    protected long getGameStartTime()
    {
        return mGameStartTime;
    }

    /**
     * Close connection as tidily as we can.
     */
    public synchronized void closeConnection()
    {
        log(LogLevel.INFO, LogType.SYSTEM, "Closing connection to %s:%s", mOptions.getHost(), mOptions.getPort());
        mIsClose = true;
    }

    /**
     * Check whether a player is allowed here.
     *
     * @param p Player to check.
     */
    private void checkPlayer(final Player p)
    {
        if (!mServerStatus.getLobby().getPlayers().contains(p))
        {
            log(LogLevel.INFO, LogType.CHECK, "Player %s steam ID %s from IP address %s left before checks performed", p.getName(), p.getSteamId(), p.getIPv4());
            return;
        }

        if (mIpLookup != null)
        {
            LoggerInterface loggerRef = this;
            new Thread(() -> p.setLocation(mIpLookup.getIPAddressInformation(p.getIPv4(), loggerRef, true).toString())).start();
        }

        if (mWhiteList.isWhitelisted(p.getSteamId()))
        {
            log(LogLevel.INFO, LogType.WHITELIST, "Player %s steam ID %s joined from IP address %s", p.getName(), p.getSteamId(), p.getIPv4());
            doGreeting(p);
            return;
        }

        if (mIpBans.isBanned(p.getIPv4()))
        {
            log(LogLevel.INFO, LogType.IPBAN, "Player %s steam ID %s joined from banned IP address %s", p.getName(), p.getSteamId(), p.getIPv4());
            banPlayer(p, "banned IP address");
            return;
        }

        if (mBanList.getBan(p.getSteamId()) != null)
        {
            final Ban banInfo = mBanList.getBan(p.getSteamId());
            log(LogLevel.INFO, LogType.PLAYERBAN, "Banned player %s steam ID %s for reason '%s' joined from IP address %s. Name at ban was %s", p.getName(), p.getSteamId(), banInfo.reason, p.getIPv4(), banInfo.name);
            banPlayer(p, banInfo.reason);
            return;
        }

        if (mOptions.isApplyVACBans() || mOptions.isApplyGameBans())
        {
            final tfa.se4.steam.json.PlayerBanInfo banInfo = mSteamAPI.getBanInfo(p.getSteamId(), p.getName(), this);
            if (banInfo != null)
            {
                if (mOptions.isApplyVACBans() && Boolean.TRUE.equals(banInfo.getVACBanned()))
                {
                    log(LogLevel.INFO, LogType.VAC, "Player %s steam ID %s has VAC ban", p.getName(), p.getSteamId());
                    banPlayer(p, "VAC ban");
                    return;
                }

                if (mOptions.isApplyGameBans() && banInfo.getNumberOfGameBans() > 0)
                {
                    log(LogLevel.INFO, LogType.GAMEBAN, "Player %s steam ID %s has game ban", p.getName(), p.getSteamId());
                    banPlayer(p, "Game ban");
                    return;
                }
            }
        }

        if (mOptions.getClosedProfilePolicy() != tfa.se4.Options.CLOSED_PROFILE_IGNORE)
        {
            final tfa.se4.steam.json.PlayerSummaryInfo info = mSteamAPI.getProfileInfo(p.getSteamId(), p.getName(), this);
            if (info != null && info.getCommunityvisibilitystate() != 3) // anything but 3 is private profile
            {
                handleClosedProfile(p);
            }
        }

        if (mOptions.getPlayHoursLimit() > 0)
        {
            final String playHours = getSteamAPI().getTotalPlaytimeHours(p.getSteamId(), p.getName(), this);
            if (playHours.equals("-"))
            {
                kickPlayer(p, "closed profile. This server is restricted to players with less than " + mOptions.getPlayHoursLimit() + " hours play time.  Please open your steam profile to allow us to view your play time.");
                return;
            }
            final int playtime = Integer.parseInt(playHours);
            if (playtime > mOptions.getPlayHoursLimit())
            {
                kickPlayer(p, "too much experience. This server is restricted to players with less than " + mOptions.getPlayHoursLimit() + " hours play time.");
                return;
            }
        }

        // Now check rules for LINKED accounts
        if (mSteamAPI != null)
        {
            final String linkedID = mSteamAPI.getLinkedProfileID(p.getSteamId(), p.getName(), this);
            if (linkedID != null)
            {
                final PlayerSummaryInfo other = mSteamAPI.getProfileInfo(linkedID, "LINK", this);
                if (other == null)
                {
                    log(LogLevel.INFO, LogType.STEAM, "Player %s steam ID %s has link to empty steam id %s", p.getName(), p.getSteamId(), linkedID);
                }
                else
                {
                    log(LogLevel.INFO, LogType.STEAM, "Player %s steam ID %s has link to %s steam id %s", p.getName(), p.getSteamId(), other.getPersonaname(), other.getSteamid());

                    if (mBanList.getBan(other.getSteamid()) != null)
                    {
                        final Ban banInfo = mBanList.getBan(other.getSteamid());
                        log(LogLevel.INFO, LogType.PLAYERBAN, "Banned player %s steam ID %s for link to banned account %s / %s original ban reason was %s", p.getName(), p.getSteamId(), other.getPersonaname(), other.getSteamid(), banInfo.reason);
                        banPlayer(p, "link to banned account " + other.getPersonaname() + " / " + other.getSteamid());
                        return;
                    }

                    if (mOptions.isApplyVACBans() || mOptions.isApplyGameBans())
                    {
                        final tfa.se4.steam.json.PlayerBanInfo linkedBanInfo = mSteamAPI.getBanInfo(other.getSteamid(), other.getPersonaname(), this);
                        if (linkedBanInfo != null)
                        {
                            if (mOptions.isApplyVACBans() && Boolean.TRUE.equals(linkedBanInfo.getVACBanned()))
                            {
                                log(LogLevel.INFO, LogType.VAC, "Player %s steam ID %s has link to VAC banned account %s / %s", p.getName(), p.getSteamId(), other.getPersonaname(), other.getSteamid());
                                banPlayer(p, "VAC ban on linked account " + other.getPersonaname() + " / " + other.getSteamid());
                                return;
                            }

                            if (mOptions.isApplyGameBans() && linkedBanInfo.getNumberOfGameBans() > 0)
                            {
                                log(LogLevel.INFO, LogType.GAMEBAN, "Player %s steam ID %s has link to game banned account %s / %s", p.getName(), p.getSteamId(), other.getPersonaname(), other.getSteamid());
                                banPlayer(p, "Game ban on linked account " + other.getPersonaname() + " / " + other.getSteamid());
                                return;
                            }
                        }
                    }
                }
            }
            else
            {
                log(LogLevel.INFO, LogType.STEAM, "Player %s steam ID %s has no link to other accounts", p.getName(), p.getSteamId());
            }
        }

        // Passed all other checks.
        doGreeting(p);
    }

    /**
     * Kick player.
     *
     * @param p      Player to kick
     * @param reason Reason
     */
    public void kickPlayer(final Player p, final String reason)
    {
        new Thread(() -> {
            // Make the kick public
            serverSay(MessageFormat.format("KICKING {0} for {1}", p.getName(), reason));

            // Delay 5 seconds
            Utils.sleep(5000);

            // Kick them by re-searching the current players list to get
            // the latest id and then kick by id.
            mServerStatus.getLobby().getPlayers().stream().filter(x -> x.equals(p)).findFirst().ifPresent(x -> {
                final String ban = "Server.KickIndex " + x.getId();
                sendMessage(Protocol.REQUEST_SEND_COMMAND, ban.getBytes());
            });
        }).start();
    }

    /**
     * Apply player ban.
     *
     * @param p      Player to ban
     * @param reason Reason
     */
    public void banPlayer(final Player p, final String reason)
    {
        new Thread(() -> {
            final boolean banAdded = mBanList.addBan(p.getSteamId(), p.getName(), reason);
            // Do a steam ID ban to make sure they are added to server ban list
            final String steamIDBan = "Server.KickBanSteamID " + p.getSteamId();
            sendMessage(Protocol.REQUEST_SEND_COMMAND, steamIDBan.getBytes());

            // Make the ban public
            final String msg = banAdded ?
                    MessageFormat.format("BANNING {0} for {1}", p.getName(), reason) :
                    MessageFormat.format("KICKING banned player {0} for {1}", p.getName(), reason);
            serverSay(msg);

            // Delay 5 seconds
            Utils.sleep(5000);

            // Kick ban them by re-searching the current players list to get
            // the latest id and then kick by id.
            mServerStatus.getLobby().getPlayers().stream().filter(x -> x.equals(p)).findFirst().ifPresent(x -> {
                final String ban = "Server.KickBanIndex " + x.getId();
                sendMessage(Protocol.REQUEST_SEND_COMMAND, ban.getBytes());
            });
        }).start();
    }

    /**
     * Handle a closed profile.
     *
     * @param p Player with closed profile
     */
    public void handleClosedProfile(final Player p)
    {
        new Thread(() -> {
            serverSay(mOptions.getClosedProfileMessage().replace("#PlayerName#", p.getName()));

            if (mOptions.getClosedProfilePolicy() == Options.CLOSED_PROFILE_KICK)
            {
                Utils.sleep(10000);
                // Kick them
                final String kick = "Server.Kick " + p.getName();
                sendMessage(Protocol.REQUEST_SEND_COMMAND, kick.getBytes());
            }
        }).start();
    }

    /**
     * Send a player greeting message (if configured)
     *
     * @param p Player to greet.
     */
    private void doGreeting(final Player p)
    {
        // Don't greet for when we've just started up or there's no greeting message
        if (StringUtils.isNotBlank(mOptions.getPlayerGreeting()) && !p.isSkipGreeting())
        {
            serverSay(mOptions.getPlayerGreeting().
                    replace("#PlayerName#", p.getName()).
                    replace("#ServerName#", mServerStatus.getServer().getName()));
        }
    }

    /**
     * Send server a command
     *
     * @param command Reason
     */
    public void sendCommand(final String command)
    {
        new Thread(() -> sendMessage(Protocol.REQUEST_SEND_COMMAND, command.getBytes())).start();
    }

    /**
     * Get URL.
     *
     * @return ws://<<host>:<port>
     * @throws URISyntaxException Shouldn't happen unless bad input
     */
    public URI getURI() throws URISyntaxException
    {
        return new URI("ws://" + mOptions.getHost() + ":" + mOptions.getPort());
    }

    /**
     * Display message in-game, splitting to multiple lines if needed.
     * @param message Message to display
     */
    private void serverSay(final String message)
    {
        for (final String s : WordUtils.wrap(message, 125).split(System.lineSeparator()))
        {
            final String msg = "Server.Say " + s;
            sendMessage(Protocol.REQUEST_SEND_COMMAND, msg.getBytes());
        }
    }

    /**
     * Update state based on receiving a {@link Protocol#REPLY_WEB_STATUS_UPDATE} notification.
     * The payload is JSON which we parse to a {@link tfa.se4.json.ServerStatus} structure.
     * <ul>
     * <li>Check for players joining</li>
     * <li>Check for players leaving</li>
     * <li>Check for change in game state</li>
     * </ul>
     *
     * @param content Payload content from incoming message.
     */
    private synchronized void setServerStatus(final ByteBuffer content)
    {
        ServerStatus status = JSONUtils.unMarshalServerStatus(Protocol.payloadAsUTF8String(content), this);
        if (status == null) // do nothing when we've got bad JSON
        {
            return;
        }

        final List<Player> newPlayers = status.getLobby().getPlayers();
        final List<Player> oldPlayers = mServerStatus == null ? new ArrayList<>() : mServerStatus.getLobby().getPlayers();

        for (final Player p : newPlayers)
        {
            if (!oldPlayers.contains(p))
            {
                if (mServerStatus == null) // signal to skip the greeting if we're just starting up the monitoring
                {
                    p.setSkipGreeting(true);
                }

                mPlayersToCheck.add(p);
                log(LogLevel.INFO, LogType.JOIN, "Player %s steam ID %s joined from IP address %s", p.getName(), p.getSteamId(), p.getIPv4());
                mPlayerJoinTime.put(p.getSteamId(), System.currentTimeMillis());
            }
        }

        for (final Player p : oldPlayers)
        {
            if (!newPlayers.contains(p))
            {
                logPlayerLeaveEvent(p, mPlayerJoinTime.getOrDefault(p.getSteamId(), System.currentTimeMillis()));
                mPlayerJoinTime.remove(p.getSteamId());
            }
        }

        final String newGameState = status.getLobby().getState();
        final String oldGameState = mServerStatus == null ? "unknown" : mServerStatus.getLobby().getState();
        if (!oldGameState.equals(newGameState))
        {
            if (Protocol.IN_GAME.equals(newGameState) && mServerStatus != null)
            {
                mGameStartTime = System.currentTimeMillis();
                log(LogLevel.INFO, LogType.GAME_STARTED, JSONUtils.marshalServerStatus(mServerStatus, this));
            }

            if (Protocol.IN_GAME.equals(oldGameState))
            {
                logGameEndEvent(mServerStatus, mGameStartTime, System.currentTimeMillis());
                mGameStartTime = 0;
            }
            else
            {
                log(LogLevel.DEBUG, LogType.GAME_DATA, Protocol.payloadAsUTF8String(content));
            }
        }

        setServerStatus(status);
    }

    /**
     * Log game ended information.
     * @param status Game state.
     * @param gameStartTime Game start time - System.currentTimeMillis() at start
     * @param gameEndTime Game end time - System.currentTimeMillis() at end
     */
    private void logGameEndEvent(final ServerStatus status, final long gameStartTime, final long gameEndTime)
    {
        if (status == null || status.getLobby() == null || status.getLobby().getPlayers() == null || status.getLobby().getPlayers().isEmpty())
        {
            return; // empty game
        }

        for (final Player p : status.getLobby().getPlayers())
        {
            if (gameStartTime == 0)
            {
                // Should only happen when tool started during a game
                p.setGamePlaySeconds(0L);
            }
            else
            {
                long start = Math.max(mPlayerJoinTime.getOrDefault(p.getSteamId(), 0L), gameStartTime);
                p.setGamePlaySeconds((gameEndTime - start) / 1000L);
            }

            setIPInformation(p);
        }
        final String postBody = JSONUtils.marshalServerStatus(status, this);
        log(LogLevel.INFO, LogType.GAME_ENDED, postBody);
    }

    /**
     * Log player leave information.
     * @param p player data
     * @param joinTime Join time
     */
    private void logPlayerLeaveEvent(final Player p, final long joinTime)
    {
        long joinedSecs = (System.currentTimeMillis() - joinTime) / 1000;

        // Blank out data we don't want to pass
        p.setAssists(null);
        p.setDeaths(null);
        p.setGamePlaySeconds(null);
        p.setId(null);
        p.setKills(null);
        p.setLatency(null);
        p.setLocation(null);
        p.setLongestShot(null);
        p.setPlayhours(null);
        p.setScore(null);
        p.setAdditionalProperty("ConnectionTimeSeconds", joinedSecs);
        p.setAdditionalProperty("Host", mServerStatus.getServer().getHost());

        setIPInformation(p);
        final String postBody = JSONUtils.marshalPlayer(p, this);
        log(LogLevel.INFO, LogType.LEAVE, postBody);
    }

    private void setIPInformation(final Player p)
    {
        if (mIpLookup != null)
        {
            final IPInformation ipInfo = mIpLookup.getIPAddressInformation(p.getIPv4(), this, false);
            if (ipInfo != null)
            {
                p.setAdditionalProperty("Latitude", ipInfo.getLatitude());
                p.setAdditionalProperty("Longitude", ipInfo.getLongitude());
                p.setAdditionalProperty("City", ipInfo.getCity());
                p.setAdditionalProperty("Region", ipInfo.getRegion());
                p.setAdditionalProperty("Country", ipInfo.getCountry());
            }
        }

    }
    /**
     * Status update handler for inherited classes to over-ride.
     *
     * @param status Server status from SE
     */
    public void setServerStatus(final ServerStatus status)
    {
        mServerStatus = status;
    }

    /**
     * Simple latch so caller will wait until this is closed.
     *
     * @throws InterruptedException Never :)
     */
    public void awaitClose() throws InterruptedException
    {
        mCloseLatch.await();
    }

    /**
     * Send message to SE4 server.
     */
    private void sendInitMessage()
    {
        final ByteBuffer buf = Protocol.buildMessage(Protocol.REQUEST_INIT);
        try
        {
            log(LogLevel.TRACE, LogType.SYSTEM, ">> %s", Protocol.bytesToString(buf.array()));
            mSession.getRemote().sendBytes(buf);
        }
        catch (final IOException ex)
        {
            log(LogLevel.TRACE, LogType.SYSTEM, ex, ex.getLocalizedMessage());
        }
    }

    /**
     * Send message with payload to SE4 server.
     *
     * @param messageId Message ID from {@link Protocol}.
     * @param msg       Message payload.
     */
    private void sendMessage(final char messageId, final byte[] msg)
    {
        final ByteBuffer buf = Protocol.buildMessage(messageId, msg);

        try
        {
            log(LogLevel.TRACE, LogType.SYSTEM, ">> %s", Protocol.bytesToString(buf.array()));
            mSession.getRemote().sendBytes(buf);
        }
        catch (final IOException ex)
        {
            log(LogLevel.TRACE, LogType.SYSTEM, ex, ex.getLocalizedMessage());
        }
    }

    /**
     * Handle income message after we've split it into ID and (optional) payload.
     *
     * @param messageId MessageId
     * @param content   Payload - may be empty
     */
    private void handleMessage(final char messageId, final ByteBuffer content)
    {
        switch (messageId)
        {
            case Protocol.REPLY_INIT:
                if (content.get(0) == 0 && Protocol.getUInt32(content, 4) == 2233321765L) {
                    log(LogLevel.INFO, LogType.SYSTEM, "Authenticating");
                    final byte[] salt = Protocol.getSaltValue(content);
                    sendMessage(Protocol.REQUEST_SEND_PWD, Protocol.buildSaltedPassword(salt, mOptions.getPassword()));
                }
                else {
                    log(LogLevel.INFO, LogType.SYSTEM, "Protocol or game version mismatch. Please ensure you are connecting to a Sniper Elite 4 server.");
                    closeConnection();
                }
                break;

            case Protocol.REPLY_CONNECTION_SUCCESS_1:
            case Protocol.REPLY_CONNECTION_SUCCESS_2:
                log(LogLevel.INFO, LogType.SYSTEM, "Authentication successful");
                sendMessage(Protocol.REQUEST_SET_WEB_STATUS_UPDATE_INTERVAL, Protocol.uInt32ToBytes(5000));
                break;

            case Protocol.REPLY_WEB_STATUS_UPDATE:
                setServerStatus(content);
                break;

            case Protocol.REPLY_ASYNC_MSG:
                final String message = Protocol.payloadAsUTF8String(content);
                log(LogLevel.INFO, LogType.MSG, message);
                break;

            case Protocol.REPLY_WEB_STATUS_MONITOR_UPDATE:
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
                log(LogLevel.INFO, LogType.SYSTEM, "Unhandled message id = %s payload raw = %s  string = %s",
                        Integer.toString(messageId),
                        Protocol.bytesToString(content.array()),
                        Protocol.payloadAsUTF8String(content));
        }
    }

    /**
     * Handle web status monitor message. It gives us updates on traffic in and out
     * and FPS.
     *
     * @param content Message payload.
     */
    private synchronized void handleWebStatusMonitorUpdate(final ByteBuffer content)
    {
        if (content.array().length == 12)
        {
            updateServerStatistics(Protocol.getUInt32(content, 0), Protocol.getUInt32(content, 4), Protocol.getFloat(content, 8));
        }
        else
        {
            log(LogLevel.ERROR, LogType.SYSTEM, "Unexpected content length for WebStatusMonitorUpdate %s", Protocol.bytesToString(content.array()));
        }
    }

    /**
     * Update server statistics method to allow inherited classes to override.
     *
     * @param bytesSent     Bytes send
     * @param bytesReceived Bytes received
     * @param fps           FPS
     */
    public void updateServerStatistics(final long bytesSent, final long bytesReceived, final float fps)
    {
        // Don't care.
    }

    /**
     * Handler for when we receive an open web socket event. We trigger the
     * password exchange handshake here.
     *
     * @param session Web socket session.
     */
    @OnWebSocketConnect
    public void onConnect(Session session)
    {
        mRetryTimeoutSecs = 10; // reset
        log(LogLevel.INFO, LogType.SYSTEM, "Connected to %s", session.getRemoteAddress().toString());
        mSession = session;
        if (mSession.isOpen())
        {
            sendInitMessage();
        }
    }

    /**
     * Handler for when we receive a close web socket event. Just close the connection.
     *
     * @param statusCode Status code returned.
     * @param reason     Reason for closure.
     */
    @OnWebSocketClose
    public void onClose(int statusCode, String reason)
    {
        log(LogLevel.INFO, LogType.SYSTEM, "Connection closed. Status code is '%s' reason is '%s", Integer.toString(statusCode), reason);
        mSession = null;
        mClient = null;
    }

    /**
     * Handler for incoming messages from SE4 server. We split the message
     * into its ID and payload then hand off to {@link #handleMessage(char, ByteBuffer)}
     * to deal with it.
     *
     * @param session Web socket session
     * @param b       Message bytes
     * @param offset  Offset into bytes, always zero
     * @param length  Length of message, always same as b.length
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
     *
     * @param cause Underlying cause.
     */
    @OnWebSocketError
    public void onError(Throwable cause)
    {
        // Remote connection died or server was killed.
        // timeout connecting
        // Broken pipe
        if (cause instanceof IOException)
        {
            log(LogLevel.INFO, LogType.SYSTEM, "WebSocket error handled as connection death");
        }
        else
        {
            log(LogLevel.ERROR, LogType.SYSTEM, cause, "Unhandled webSocket Error. Treating as fatal");
        }
        mSession = null;
        mClient = null;
    }

    @Override
    public void log(LogLevel level, LogType type, String message, Object... args)
    {
        log(level, type, null, message, args);
    }

    @Override
    public void log(LogLevel level, LogType type, Throwable t, String message, Object... args)
    {
        if (isFilterMessage(level))
        {
            return;
        }

        System.out.println(Utils.formatLogMessage(level, type, t, message, args)); //NOSONAR
    }

    /**
     * Whether to filter out messages of the specified log level.
     *
     * @param level Log level to check
     * @return true if NOT to display it.
     */
    protected boolean isFilterMessage(final LogLevel level)
    {
        if (level.equals(LogLevel.ERROR))
        {
            return false; // always show ERROR
        }

        switch (mDisplayLogLevel)
        {
            case INFO:
                return level.equals(LogLevel.DEBUG) || level.equals(LogLevel.TRACE);
            case DEBUG:
                return level.equals(LogLevel.TRACE);
            case TRACE:
            default:
                return false;
        }
    }

    public static void main(String[] args) throws Exception
    {
        if (args.length != 1)
        {
            System.out.println("Usage:\njava tfa.se4.SEAdminServerConnection <config file>"); //NOSONAR
            System.exit(0);
        }

        final Options opts = new Options(args[0]);
        if (opts.hasInvalidSteamSettings())
        {
            opts.makeSteamSettingsConsistent();
            System.out.println("Kicking of VAC or Game banned player, managing closed profiles and limiting play hours requires a steam API key for checks to be performed. These features will be disabled.  Please update your configuration."); //NOSONAR
        }
        final SEAdminServerConnection socket = new SEAdminServerConnection(opts);
        socket.awaitClose();
    }

}
