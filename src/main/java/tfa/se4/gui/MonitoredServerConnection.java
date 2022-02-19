package tfa.se4.gui;

import javafx.application.Platform;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import tfa.se4.Options;
import tfa.se4.SEAdminServerConnection;
import tfa.se4.Utils;
import tfa.se4.json.ServerStatus;

import java.util.concurrent.TimeUnit;

@WebSocket
public class MonitoredServerConnection extends SEAdminServerConnection
{

    private final GameModel model;

    /**
     * Set up and manage connection based on property configuration.
     *
     * @param options Configuration options.
     */
    public MonitoredServerConnection(Options options)
    {
        super(options);
        model = new GameModel();
        if (isSteamNotAvailable())
        {
            log(LogLevel.INFO, LogType.STEAM, "Steam access is disabled. No automatic ban checks will be performed.");
        }

        if (ipLookupNotAvailable())
        {
            log(LogLevel.INFO, LogType.IPINFO, "IP address checking is disabled. No location lookups will be performed.");
        }
    }

    @Override
    public void setServerStatus(ServerStatus status)
    {
        super.setServerStatus(status);
        if (status == null) {
            model.setTimeLeft(null);
            return;
        }

        Platform.runLater(() -> {
            if (status.getGameData() != null && status.getGameData().getCurrentMap() != null)
            {
                model.setMap(status.getGameData().getCurrentMap().getName());
                model.setMode(status.getGameData().getCurrentMap().getMode());
                model.setScoreLimit(status.getGameData().getCurrentMap().getScoreLimit().toString());
                model.setTimeLimit(status.getGameData().getCurrentMap().getTimeLimit().toString());
            }

            if (status.getLobby() != null)
            {
                model.setState(status.getLobby().getState());
                model.setMaxPlayers(status.getLobby().getMaxPlayers().toString());
                if (getIPLookup() != null)
                {
                    status.getLobby().getPlayers().forEach(p -> p.setLocation(getIPLookup().getIPAddressInformation(p.getIPv4(), this, false).toString()));
                }

                if (getSteamAPI() != null)
                {
                    status.getLobby().getPlayers().forEach(p -> p.setPlayhours(getSteamAPI().getTotalPlaytimeHours(p.getSteamId(), p.getName(), this)));
                }
                model.setPlayers(status.getLobby().getPlayers());
            }

            if (status.getServer() != null)
            {
                model.setServerName(status.getServer().getName());
                model.setServerHost(status.getServer().getHost());
            }

            if (getGameStartTime() == 0)
            {
                model.setTimeLeft(null);
            }
            else
            {
                final long endTime = getGameStartTime() + status.getGameData().getCurrentMap().getTimeLimit() * 60000;
                model.setTimeLeft(formatTimeLeft(endTime - System.currentTimeMillis()));
            }
        });
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

        Platform.runLater(() -> model.addLogLine(Utils.formatLogMessage(level, type, t, message, args)));
    }

    @Override
    public void updateServerStatistics(long bytesSent, long bytesReceived, float fps)
    {
        super.updateServerStatistics(bytesSent, bytesReceived, fps);

        Platform.runLater(() -> {
            model.setBytesSent(formatBytes(bytesSent));
            model.setBytesReceived(formatBytes(bytesReceived));
            model.setFps(Integer.toString(Math.round(fps)));

        });
    }

    /**
     * Is IP address lookup supported
     *
     * @return Whether IP stack is in use.
     */
    public boolean ipLookupNotAvailable()
    {
        return getIPLookup() == null;

    }

    /**
     * Is steam in use ?
     *
     * @return Whether steam is in use
     */
    public boolean isSteamNotAvailable()
    {
        return getSteamAPI() == null;
    }

    /**
     * Pretty print bytes values.
     *
     * @param bytes bytes
     * @return pretty printed bytes
     */
    private String formatBytes(final long bytes)
    {
        if (bytes < 1024)
        {
            return String.format("%d byte", bytes);
        }
        else if (bytes < (1024 * 1024))
        {
            return String.format("%.2f kb", bytes / 1024.0);
        }
        else if (bytes < (1024 * 1024 * 1024))
        {
            return String.format("%.2f mb", bytes / (1024.0 * 1024.0));
        }
        else
        {
            return String.format("%.2f gb", bytes / (1024.0 * 1024.0 * 1024.0));
        }
    }

    private String formatTimeLeft(final long millis)
    {
        final long hours = TimeUnit.MILLISECONDS.toHours(millis);
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis));
        final long secs = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis));
        if (hours > 0)
        {
            return String.format("%02d:%02d:%02d", hours, minutes, secs);
        }
        else if (minutes > 0)
        {
            return String.format("%02d:%02d", minutes, secs);
        }
        else
        {
            return String.format("%02d", secs);
        }
    }

    public GameModel getModel()
    {
        return model;
    }
}
