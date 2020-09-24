package tfa.se4.ipstack.json;

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
        "ip",
        "type",
        "continent_code",
        "continent_name",
        "country_code",
        "country_name",
        "region_code",
        "region_name",
        "city",
        "zip",
        "latitude",
        "longitude",
        "location"
})
public class IPStack
{

    @JsonProperty("ip")
    private String ip;
    @JsonProperty("type")
    private String type;
    @JsonProperty("continent_code")
    private String continentCode;
    @JsonProperty("continent_name")
    private String continentName;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("country_name")
    private String countryName;
    @JsonProperty("region_code")
    private String regionCode;
    @JsonProperty("region_name")
    private String regionName;
    @JsonProperty("city")
    private String city;
    @JsonProperty("zip")
    private String zip;
    @JsonProperty("latitude")
    private Double latitude;
    @JsonProperty("longitude")
    private Double longitude;
    @JsonProperty("location")
    private Location location;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("ip")
    public String getIp()
    {
        return ip;
    }

    @JsonProperty("ip")
    public void setIp(String ip)
    {
        this.ip = ip;
    }

    @JsonProperty("type")
    public String getType()
    {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type)
    {
        this.type = type;
    }

    @JsonProperty("continent_code")
    public String getContinentCode()
    {
        return continentCode;
    }

    @JsonProperty("continent_code")
    public void setContinentCode(String continentCode)
    {
        this.continentCode = continentCode;
    }

    @JsonProperty("continent_name")
    public String getContinentName()
    {
        return continentName;
    }

    @JsonProperty("continent_name")
    public void setContinentName(String continentName)
    {
        this.continentName = continentName;
    }

    @JsonProperty("country_code")
    public String getCountryCode()
    {
        return countryCode;
    }

    @JsonProperty("country_code")
    public void setCountryCode(String countryCode)
    {
        this.countryCode = countryCode;
    }

    @JsonProperty("country_name")
    public String getCountryName()
    {
        return countryName;
    }

    @JsonProperty("country_name")
    public void setCountryName(String countryName)
    {
        this.countryName = countryName;
    }

    @JsonProperty("region_code")
    public String getRegionCode()
    {
        return regionCode;
    }

    @JsonProperty("region_code")
    public void setRegionCode(String regionCode)
    {
        this.regionCode = regionCode;
    }

    @JsonProperty("region_name")
    public String getRegionName()
    {
        return regionName;
    }

    @JsonProperty("region_name")
    public void setRegionName(String regionName)
    {
        this.regionName = regionName;
    }

    @JsonProperty("city")
    public String getCity()
    {
        return city;
    }

    @JsonProperty("city")
    public void setCity(String city)
    {
        this.city = city;
    }

    @JsonProperty("zip")
    public String getZip()
    {
        return zip;
    }

    @JsonProperty("zip")
    public void setZip(String zip)
    {
        this.zip = zip;
    }

    @JsonProperty("latitude")
    public Double getLatitude()
    {
        return latitude;
    }

    @JsonProperty("latitude")
    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    @JsonProperty("longitude")
    public Double getLongitude()
    {
        return longitude;
    }

    @JsonProperty("longitude")
    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    @JsonProperty("location")
    public Location getLocation()
    {
        return location;
    }

    @JsonProperty("location")
    public void setLocation(Location location)
    {
        this.location = location;
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