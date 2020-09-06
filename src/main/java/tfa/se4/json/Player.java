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
@JsonPropertyOrder({ "Id", "Name", "SteamId", "IPv4", "Latency", "Score", "Deaths", "Kills", "Assists", "LongestShot" })
public class Player {

	@JsonProperty("Id")
	private String id;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("SteamId")
	private String steamId;
	@JsonProperty("IPv4")
	private String iPv4;
	@JsonProperty("Latency")
	private Integer latency;
	@JsonProperty("Score")
	private Integer score;
	@JsonProperty("Deaths")
	private Integer deaths;
	@JsonProperty("Kills")
	private Integer kills;
	@JsonProperty("Assists")
	private Integer assists;
	@JsonProperty("LongestShot")
	private Double longestShot;
	@JsonIgnore
	private Map<String, Object> additionalProperties = new HashMap<String, Object>();

	private boolean skipGreeting;
	@JsonProperty("Id")
	public String getId() {
		return id;
	}

	@JsonProperty("Id")
	public void setId(String id) {
		this.id = id;
	}

	@JsonProperty("Name")
	public String getName() {
		return name;
	}

	@JsonProperty("Name")
	public void setName(String name) {
		this.name = name;
	}

	@JsonProperty("SteamId")
	public String getSteamId() {
		return steamId;
	}

	@JsonProperty("SteamId")
	public void setSteamId(String steamId) {
		this.steamId = steamId;
	}

	@JsonProperty("IPv4")
	public String getIPv4() {
		return iPv4;
	}

	@JsonProperty("IPv4")
	public void setIPv4(String iPv4) {
		this.iPv4 = iPv4;
	}

	@JsonProperty("Latency")
	public Integer getLatency() {
		return latency;
	}

	@JsonProperty("Latency")
	public void setLatency(Integer latency) {
		this.latency = latency;
	}

	@JsonProperty("Score")
	public Integer getScore() {
		return score;
	}

	@JsonProperty("Score")
	public void setScore(Integer score) {
		this.score = score;
	}

	@JsonProperty("Deaths")
	public Integer getDeaths() {
		return deaths;
	}

	@JsonProperty("Deaths")
	public void setDeaths(Integer deaths) {
		this.deaths = deaths;
	}

	@JsonProperty("Kills")
	public Integer getKills() {
		return kills;
	}

	@JsonProperty("Kills")
	public void setKills(Integer kills) {
		this.kills = kills;
	}

	@JsonProperty("Assists")
	public Integer getAssists() {
		return assists;
	}

	@JsonProperty("Assists")
	public void setAssists(Integer assists) {
		this.assists = assists;
	}

	@JsonProperty("LongestShot")
	public Double getLongestShot() {
		return longestShot;
	}

	@JsonProperty("LongestShot")
	public void setLongestShot(Double longestShot) {
		this.longestShot = longestShot;
	}

	@JsonAnyGetter
	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	@JsonAnySetter
	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

    public boolean isSkipGreeting() {
        return skipGreeting;
    }

    public void setSkipGreeting(boolean skipGreeting) {
        this.skipGreeting = skipGreeting;
    }
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((steamId == null) ? 0 : steamId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Player other = (Player) obj;
		if (steamId == null) {
			if (other.steamId != null)
				return false;
		} else if (!steamId.equals(other.steamId))
			return false;
		return true;
	}
}
