package tfa.se4.iplookup.extremeip;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "businessName",
    "businessWebsite",
    "city",
    "continent",
    "country",
    "countryCode",
    "ipName",
    "ipType",
    "isp",
    "lat",
    "lon",
    "org",
    "query",
    "region",
    "status",
    "timezone",
    "utcOffset"
})

public class ExtremeIPResponse {
    @JsonProperty("businessName")
    private String businessName;
    @JsonProperty("businessWebsite")
    private String businessWebsite;
    @JsonProperty("city")
    private String city;
    @JsonProperty("continent")
    private String continent;
    @JsonProperty("country")
    private String country;
    @JsonProperty("countryCode")
    private String countryCode;
    @JsonProperty("ipName")
    private String ipName;
    @JsonProperty("ipType")
    private String ipType;
    @JsonProperty("isp")
    private String isp;
    @JsonProperty("lat")
    private String lat;
    @JsonProperty("lon")
    private String lon;
    @JsonProperty("org")
    private String org;
    @JsonProperty("query")
    private String query;
    @JsonProperty("region")
    private String region;
    @JsonProperty("status")
    private String status;
    @JsonProperty("timezone")
    private String timezone;
    @JsonProperty("utcOffset")
    private String utcOffset;

    @JsonProperty("businessName")
    public String getBusinessName() {
        return businessName;
    }

    @JsonProperty("businessName")
    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    @JsonProperty("businessWebsite")
    public String getBusinessWebsite() {
        return businessWebsite;
    }

    @JsonProperty("businessWebsite")
    public void setBusinessWebsite(String businessWebsite) {
        this.businessWebsite = businessWebsite;
    }

    @JsonProperty("city")
    public String getCity() {
        return city;
    }

    @JsonProperty("city")
    public void setCity(String city) {
        this.city = city;
    }

    @JsonProperty("continent")
    public String getContinent() {
        return continent;
    }

    @JsonProperty("continent")
    public void setContinent(String continent) {
        this.continent = continent;
    }

    @JsonProperty("country")
    public String getCountry() {
        return country;
    }

    @JsonProperty("country")
    public void setCountry(String country) {
        this.country = country;
    }

    @JsonProperty("countryCode")
    public String getCountryCode() {
        return countryCode;
    }

    @JsonProperty("countryCode")
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    @JsonProperty("ipName")
    public String getIpName() {
        return ipName;
    }

    @JsonProperty("ipName")
    public void setIpName(String ipName) {
        this.ipName = ipName;
    }

    @JsonProperty("ipType")
    public String getIpType() {
        return ipType;
    }

    @JsonProperty("ipType")
    public void setIpType(String ipType) {
        this.ipType = ipType;
    }

    @JsonProperty("isp")
    public String getIsp() {
        return isp;
    }

    @JsonProperty("isp")
    public void setIsp(String isp) {
        this.isp = isp;
    }

    @JsonProperty("lat")
    public String getLat() {
        return lat;
    }

    @JsonProperty("lat")
    public void setLat(String lat) {
        this.lat = lat;
    }

    @JsonProperty("lon")
    public String getLon() {
        return lon;
    }

    @JsonProperty("lon")
    public void setLon(String lon) {
        this.lon = lon;
    }

    @JsonProperty("org")
    public String getOrg() {
        return org;
    }

    @JsonProperty("org")
    public void setOrg(String org) {
        this.org = org;
    }

    @JsonProperty("query")
    public String getQuery() {
        return query;
    }

    @JsonProperty("query")
    public void setQuery(String query) {
        this.query = query;
    }

    @JsonProperty("region")
    public String getRegion() {
        return region;
    }

    @JsonProperty("region")
    public void setRegion(String region) {
        this.region = region;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("status")
    public void setStatus(String status) {
        this.status = status;
    }

    @JsonProperty("timezone")
    public String getTimezone() {
        return timezone;
    }

    @JsonProperty("timezone")
    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    @JsonProperty("utcOffset")
    public String getUtcOffset() {
        return utcOffset;
    }

    @JsonProperty("utcOffset")
    public void setUtcOffset(String utcOffset) {
        this.utcOffset = utcOffset;
    }
}

