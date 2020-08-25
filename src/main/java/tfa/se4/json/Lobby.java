package tfa.se4.json;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "State", "MaxPlayers", "Players" })
public class Lobby {

	@JsonProperty("State")
	private String state;
	@JsonProperty("MaxPlayers")
	private Integer maxPlayers;
	@JsonProperty("Players")
	private List<Player> players = null;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	@JsonProperty("State")
	public String getState() {
		return state;
	}

	@JsonProperty("State")
	public void setState(String state) {
		this.state = state;
	}

	@JsonProperty("MaxPlayers")
	public Integer getMaxPlayers() {
		return maxPlayers;
	}

	@JsonProperty("MaxPlayers")
	public void setMaxPlayers(Integer maxPlayers) {
		this.maxPlayers = maxPlayers;
	}

	@JsonProperty("Players")
	public List<Player> getPlayers() {
		return players;
	}

	@JsonProperty("Players")
	public void setPlayers(List<Player> players) {
		this.players = players;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

}
