package tfa.se4.steam.json;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
"SteamId",
"CommunityBanned",
"VACBanned",
"NumberOfVACBans",
"DaysSinceLastBan",
"NumberOfGameBans",
"EconomyBan"
})
public class Player {

@JsonProperty("SteamId")
private String steamId;
@JsonProperty("CommunityBanned")
private Boolean communityBanned;
@JsonProperty("VACBanned")
private Boolean vACBanned;
@JsonProperty("NumberOfVACBans")
private Integer numberOfVACBans;
@JsonProperty("DaysSinceLastBan")
private Integer daysSinceLastBan;
@JsonProperty("NumberOfGameBans")
private Integer numberOfGameBans;
@JsonProperty("EconomyBan")
private String economyBan;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<String, Object>();

@JsonProperty("SteamId")
public String getSteamId() {
return steamId;
}

@JsonProperty("SteamId")
public void setSteamId(String steamId) {
this.steamId = steamId;
}

@JsonProperty("CommunityBanned")
public Boolean getCommunityBanned() {
return communityBanned;
}

@JsonProperty("CommunityBanned")
public void setCommunityBanned(Boolean communityBanned) {
this.communityBanned = communityBanned;
}

@JsonProperty("VACBanned")
public Boolean getVACBanned() {
return vACBanned;
}

@JsonProperty("VACBanned")
public void setVACBanned(Boolean vACBanned) {
this.vACBanned = vACBanned;
}

@JsonProperty("NumberOfVACBans")
public Integer getNumberOfVACBans() {
return numberOfVACBans;
}

@JsonProperty("NumberOfVACBans")
public void setNumberOfVACBans(Integer numberOfVACBans) {
this.numberOfVACBans = numberOfVACBans;
}

@JsonProperty("DaysSinceLastBan")
public Integer getDaysSinceLastBan() {
return daysSinceLastBan;
}

@JsonProperty("DaysSinceLastBan")
public void setDaysSinceLastBan(Integer daysSinceLastBan) {
this.daysSinceLastBan = daysSinceLastBan;
}

@JsonProperty("NumberOfGameBans")
public Integer getNumberOfGameBans() {
return numberOfGameBans;
}

@JsonProperty("NumberOfGameBans")
public void setNumberOfGameBans(Integer numberOfGameBans) {
this.numberOfGameBans = numberOfGameBans;
}

@JsonProperty("EconomyBan")
public String getEconomyBan() {
return economyBan;
}

@JsonProperty("EconomyBan")
public void setEconomyBan(String economyBan) {
this.economyBan = economyBan;
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