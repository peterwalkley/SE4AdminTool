package tfa.se4.json;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"Server", "Version", "Build", "Lobby", "GameData"})
public class ServerStatus
{

    @JsonProperty("Server")
    private Server server;
    @JsonProperty("Version")
    private String version;
    @JsonProperty("Build")
    private String build;
    @JsonProperty("Lobby")
    private Lobby lobby;
    @JsonProperty("GameData")
    private GameData gameData;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("Server")
    public Server getServer()
    {
        return server;
    }

    @JsonProperty("Server")
    public void setServer(Server server)
    {
        this.server = server;
    }

    @JsonProperty("Version")
    public String getVersion()
    {
        return version;
    }

    @JsonProperty("Version")
    public void setVersion(String version)
    {
        this.version = version;
    }

    @JsonProperty("Build")
    public String getBuild()
    {
        return build;
    }

    @JsonProperty("Build")
    public void setBuild(String build)
    {
        this.build = build;
    }

    @JsonProperty("Lobby")
    public Lobby getLobby()
    {
        return lobby;
    }

    @JsonProperty("Lobby")
    public void setLobby(Lobby lobby)
    {
        this.lobby = lobby;
    }

    @JsonProperty("GameData")
    public GameData getGameData()
    {
        return gameData;
    }

    @JsonProperty("GameData")
    public void setGameData(GameData gameData)
    {
        this.gameData = gameData;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties()
    {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value)
    {
        this.additionalProperties.put(name, value);
    }
}
