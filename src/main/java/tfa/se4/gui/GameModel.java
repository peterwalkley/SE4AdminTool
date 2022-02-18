package tfa.se4.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import tfa.se4.json.Player;

import java.util.List;

/**
 * Model data for current game state.
 */
public class GameModel
{
    public static final String UNKNOWN = "unknown";

    private final StringProperty serverName = new SimpleStringProperty(this, "serverName", UNKNOWN);
    private final StringProperty serverHost = new SimpleStringProperty(this, "serverHost", UNKNOWN);
    private final StringProperty map = new SimpleStringProperty(this, "map", UNKNOWN);
    private final StringProperty mode = new SimpleStringProperty(this, "mode", UNKNOWN);
    private final StringProperty state = new SimpleStringProperty(this, "state", UNKNOWN);
    private final StringProperty timeLimit = new SimpleStringProperty(this, "timeLimit", UNKNOWN);
    private final StringProperty scoreLimit = new SimpleStringProperty(this, "scoreLimit", UNKNOWN);
    private final StringProperty maxPlayers = new SimpleStringProperty(this, "maxPlayers", UNKNOWN);
    private final StringProperty bytesSent = new SimpleStringProperty(this, "bytesSent", UNKNOWN);
    private final StringProperty bytesReceived = new SimpleStringProperty(this, "bytesReceived", UNKNOWN);
    private final StringProperty timeLeft = new SimpleStringProperty(this, "timeLeft", UNKNOWN);
    private final StringProperty fps = new SimpleStringProperty(this, "fps", UNKNOWN);

    private final ObservableList<Player> players = FXCollections.observableArrayList();
    private final ObservableList<String> logLines = FXCollections.observableArrayList();

    public void setPlayers(final List<Player> newPlayers)
    {
        players.clear();
        if (newPlayers != null)
        {
            players.addAll(newPlayers);
        }
    }

    public ObservableList<Player> getPlayers()
    {
        return players;
    }

    public void addLogLine(final String s)
    {
        logLines.add(s);
        while (logLines.size() > 10000)
            logLines.remove(0);
    }

    public void clearLog()
    {
        logLines.clear();
    }

    public List<String> getRawLogLines()
    {
        return logLines;
    }

    public ObservableList<String> getLogLines()
    {
        return logLines;
    }

    public String getServerName()
    {
        return serverName.get();
    }

    public StringProperty serverNameProperty()
    {
        return serverName;
    }

    public void setServerName(String serverName)
    {
        this.serverName.set(serverName);
    }

    public String getServerHost()
    {
        return serverHost.get();
    }

    public StringProperty serverHostProperty()
    {
        return serverHost;
    }

    public void setServerHost(String serverHost)
    {
        this.serverHost.set(serverHost);
    }

    public String getBytesSent()
    {
        return bytesSent.get();
    }

    public StringProperty bytesSentProperty()
    {
        return bytesSent;
    }

    public void setBytesSent(String bytesSent)
    {
        this.bytesSent.set(bytesSent);
    }

    public String getBytesReceived()
    {
        return bytesReceived.get();
    }

    public StringProperty bytesReceivedProperty()
    {
        return bytesReceived;
    }

    public void setBytesReceived(String bytesReceived)
    {
        this.bytesReceived.set(bytesReceived);
    }

    public String getFps()
    {
        return fps.get();
    }

    public StringProperty fpsProperty()
    {
        return fps;
    }

    public void setFps(String fps)
    {
        this.fps.set(fps);
    }

    public String getMode()
    {
        return mode.get();
    }

    public StringProperty modeProperty()
    {
        return mode;
    }

    public void setMode(String mode)
    {
        this.mode.set(mode);
    }

    public String getState()
    {
        return state.get();
    }

    public StringProperty stateProperty()
    {
        return state;
    }

    public void setState(String state)
    {
        this.state.set(state);
    }

    public String getTimeLimit()
    {
        return timeLimit.get();
    }

    public StringProperty timeLimitProperty()
    {
        return timeLimit;
    }

    public void setTimeLimit(String timeLimit)
    {
        this.timeLimit.set(timeLimit);
    }

    public String getScoreLimit()
    {
        return scoreLimit.get();
    }

    public StringProperty scoreLimitProperty()
    {
        return scoreLimit;
    }

    public void setScoreLimit(String scoreLimit)
    {
        this.scoreLimit.set(scoreLimit);
    }

    public String getMaxPlayers()
    {
        return maxPlayers.get();
    }

    public StringProperty maxPlayersProperty()
    {
        return maxPlayers;
    }

    public void setMaxPlayers(String maxPlayers)
    {
        this.maxPlayers.set(maxPlayers);
    }

    public String getMap()
    {
        return map.get();
    }

    public StringProperty mapProperty()
    {
        return map;
    }

    public void setMap(String map)
    {
        this.map.set(map);
    }

    public String getTimeLeft()
    {
        return timeLeft.get();
    }

    public StringProperty timeLeftProperty()
    {
        return timeLeft;
    }

    public void setTimeLeft(String timeLeft)
    {
        this.timeLeft.set(timeLeft);
    }

}
