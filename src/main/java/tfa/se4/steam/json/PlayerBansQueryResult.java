package tfa.se4.steam.json;

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
@JsonPropertyOrder({
"players"
})
public class PlayerBansQueryResult {

@JsonProperty("players")
private List<Player> players = null;
@JsonIgnore
private Map<String, Object> additionalProperties = new HashMap<>();

@JsonProperty("players")
public List<Player> getPlayers() {
return players;
}

@JsonProperty("players")
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
