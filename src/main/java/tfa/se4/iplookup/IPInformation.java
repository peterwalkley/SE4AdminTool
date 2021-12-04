package tfa.se4.iplookup;

import org.apache.commons.lang3.StringUtils;

public class IPInformation {
    private String latitude;
    private String longitude;
    private String city;
    private String region;
    private String country;
    private String ipAddress;

    public static final IPInformation create() {
        return new IPInformation();
    }
    public String getLatitude() {
        return latitude;
    }

    public IPInformation setLatitude(String latitude) {
        this.latitude = latitude;
        return this;
    }

    public String getLongitude() {
        return longitude;
    }

    public IPInformation setLongitude(String longitude) {
        this.longitude = longitude;
        return this;
    }

    public String getCity() {
        return city;
    }

    public IPInformation setCity(String city) {
        this.city = city;
        return this;
    }

    public String getRegion() {
        return region;
    }

    public IPInformation setRegion(String region) {
        this.region = region;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public IPInformation setCountry(String country) {
        if (StringUtils.isNotBlank(country)) // don't allow overwite of "N/A" with a blank
            this.country = country;
        return this;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public IPInformation setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
        return this;
    }

    @Override
    public String toString() {
        if (StringUtils.isBlank(country))
            return ipAddress;

        final StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(city))
        {
            sb.append(city);
            sb.append(',');
        }
        if (StringUtils.isNotBlank(region))
        {
            sb.append(region);
            sb.append(',');
        }
        sb.append(country);
        return sb.toString();
    }

}
