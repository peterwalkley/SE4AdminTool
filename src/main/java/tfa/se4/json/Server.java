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
@JsonPropertyOrder({"Name", "Host"})
public class Server
{

    @JsonProperty("Name")
    private String name;
    @JsonProperty("Host")
    private String host;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<>();

    @JsonProperty("Name")
    public String getName()
    {
        return name;
    }

    @JsonProperty("Name")
    public void setName(String name)
    {
        this.name = name;
    }

    @JsonProperty("Host")
    public String getHost()
    {
        return host;
    }

    @JsonProperty("Host")
    public void setHost(String host)
    {
        this.host = host;
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