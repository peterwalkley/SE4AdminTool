package tfa.se4.gui;

import javafx.application.Platform;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import tfa.se4.Options;
import tfa.se4.SEAdminServerConnection;
import tfa.se4.json.ServerStatus;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@WebSocket
public class MonitoredServerConnection extends SEAdminServerConnection {

    private GameModel model;

    /**
     * Set up and manage connection based on properties configuration.
     *
     * @param options Configuration options.
     */
    public MonitoredServerConnection(Options options) {
        super(options);
        model = new GameModel();
    }

    @Override
    public void setServerStatus(ServerStatus status) {
        super.setServerStatus(status);

        Platform.runLater(() -> {
            if (status != null && status.getGameData() != null && status.getGameData().getCurrentMap() != null) {
                model.setMap(status.getGameData().getCurrentMap().getName());
                model.setMode(status.getGameData().getCurrentMap().getMode());
                model.setScoreLimit(status.getGameData().getCurrentMap().getScoreLimit().toString());
                model.setTimeLimit(status.getGameData().getCurrentMap().getTimeLimit().toString());
            }

            if (status != null && status.getLobby() != null) {
                model.setState(status.getLobby().getState());
                model.setMaxPlayers(status.getLobby().getMaxPlayers().toString());
                model.setPlayers(status.getLobby().getPlayers());
            }

            if (status != null && status.getServer() != null) {
                model.setServerName(status.getServer().getName());
                model.setServerHost(status.getServer().getHost());
            }
        });
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

        Platform.runLater(() -> model.addLogLine(sb.toString()));
    }

    @Override
    public void updateServerStatistics(long bytesSent, long bytesReceived, float fps) {
        super.updateServerStatistics(bytesSent, bytesReceived, fps);

        Platform.runLater(() -> {
            model.setBytesSent(formatBytes(bytesSent));
            model.setBytesReceived(formatBytes(bytesReceived));
            model.setFps(Integer.toString(Math.round(fps)));

        });
    }

    /**
     * Pretty print bytes values.
     * @param bytes bytes
     * @return pretty printed bytes
     */
    private String formatBytes(final long bytes)
    {
        if (bytes < 1024)
            return String.format("%d byte", bytes);
        else if (bytes < (1024 * 1024))
            return String.format("%.2f kb", (float)bytes / 1024.0);
        else if (bytes < (1024 * 1024 * 1024))
            return String.format("%.2f mb", (float)bytes / (1024.0 * 1024.0));
        else
            return String.format("%.2f gb", (float)bytes / (1024.0 * 1024.0 * 1024.0));
    }

    public GameModel getModel()
    {
        return model;
    }
}
