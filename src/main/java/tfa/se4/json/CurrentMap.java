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
@JsonPropertyOrder({ "TimeLimit", "ScoreLimit", "Name", "Mode", "MaxPlayers" })
public class CurrentMap {

	@JsonProperty("TimeLimit")
	private Integer timeLimit;
	@JsonProperty("ScoreLimit")
	private Integer scoreLimit;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("Mode")
	private String mode;
	@JsonProperty("MaxPlayers")
	private Integer maxPlayers;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<>();

	@JsonProperty("TimeLimit")
	public Integer getTimeLimit() {
		return timeLimit;
	}

	@JsonProperty("TimeLimit")
	public void setTimeLimit(Integer timeLimit) {
		this.timeLimit = timeLimit;
	}

	@JsonProperty("ScoreLimit")
	public Integer getScoreLimit() {
		return scoreLimit;
	}

	@JsonProperty("ScoreLimit")
	public void setScoreLimit(Integer scoreLimit) {
		this.scoreLimit = scoreLimit;
	}

	@JsonProperty("Name")
	public String getName() {
		return name;
	}

	@JsonProperty("Name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("Mode")
	public String getMode() {
		return mode;
	}

	@JsonProperty("Mode")
	public void setMode(String mode) {
		this.mode = mode;
	}

	@JsonProperty("MaxPlayers")
	public Integer getMaxPlayers() {
		return maxPlayers;
	}

	@JsonProperty("MaxPlayers")
	public void setMaxPlayers(Integer maxPlayers) {
		this.maxPlayers = maxPlayers;
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
