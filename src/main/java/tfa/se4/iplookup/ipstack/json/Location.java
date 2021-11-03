package tfa.se4.iplookup.ipstack.json;

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
        "geoname_id",
        "capital",
        "languages",
        "country_flag",
        "country_flag_emoji",
        "country_flag_emoji_unicode",
        "calling_code",
        "is_eu"
})
public class Location
{

    @JsonProperty("geoname_id")
    private Integer geonameId;
    @JsonProperty("capital")
    private String capital;
    @JsonProperty("languages")
    private List<Language> languages = null;
    @JsonProperty("country_flag")
    private String countryFlag;
    @JsonProperty("country_flag_emoji")
    private String countryFlagEmoji;
    @JsonProperty("country_flag_emoji_unicode")
    private String countryFlagEmojiUnicode;
    @JsonProperty("calling_code")
    private String callingCode;
    @JsonProperty("is_eu")
    private Boolean isEu;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("geoname_id")
    public Integer getGeonameId()
    {
        return geonameId;
    }

    @JsonProperty("geoname_id")
    public void setGeonameId(Integer geonameId)
    {
        this.geonameId = geonameId;
    }

    @JsonProperty("capital")
    public String getCapital()
    {
        return capital;
    }

    @JsonProperty("capital")
    public void setCapital(String capital)
    {
        this.capital = capital;
    }

    @JsonProperty("languages")
    public List<Language> getLanguages()
    {
        return languages;
    }

    @JsonProperty("languages")
    public void setLanguages(List<Language> languages)
    {
        this.languages = languages;
    }

    @JsonProperty("country_flag")
    public String getCountryFlag()
    {
        return countryFlag;
    }

    @JsonProperty("country_flag")
    public void setCountryFlag(String countryFlag)
    {
        this.countryFlag = countryFlag;
    }

    @JsonProperty("country_flag_emoji")
    public String getCountryFlagEmoji()
    {
        return countryFlagEmoji;
    }

    @JsonProperty("country_flag_emoji")
    public void setCountryFlagEmoji(String countryFlagEmoji)
    {
        this.countryFlagEmoji = countryFlagEmoji;
    }

    @JsonProperty("country_flag_emoji_unicode")
    public String getCountryFlagEmojiUnicode()
    {
        return countryFlagEmojiUnicode;
    }

    @JsonProperty("country_flag_emoji_unicode")
    public void setCountryFlagEmojiUnicode(String countryFlagEmojiUnicode)
    {
        this.countryFlagEmojiUnicode = countryFlagEmojiUnicode;
    }

    @JsonProperty("calling_code")
    public String getCallingCode()
    {
        return callingCode;
    }

    @JsonProperty("calling_code")
    public void setCallingCode(String callingCode)
    {
        this.callingCode = callingCode;
    }

    @JsonProperty("is_eu")
    public Boolean getIsEu()
    {
        return isEu;
    }

    @JsonProperty("is_eu")
    public void setIsEu(Boolean isEu)
    {
        this.isEu = isEu;
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
