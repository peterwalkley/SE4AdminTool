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
        "steamid",
        "communityvisibilitystate",
        "profilestate",
        "personaname",
        "commentpermission",
        "profileurl",
        "avatar",
        "avatarmedium",
        "avatarfull",
        "avatarhash",
        "personastate",
        "realname",
        "primaryclanid",
        "timecreated",
        "personastateflags",
        "gameextrainfo",
        "gameid",
        "lobbysteamid",
        "loccountrycode"
})
public class PlayerSummaryInfo {

    @JsonProperty("steamid")
    private String steamid;
    @JsonProperty("communityvisibilitystate")
    private Integer communityvisibilitystate;
    @JsonProperty("profilestate")
    private Integer profilestate;
    @JsonProperty("personaname")
    private String personaname;
    @JsonProperty("commentpermission")
    private Integer commentpermission;
    @JsonProperty("profileurl")
    private String profileurl;
    @JsonProperty("avatar")
    private String avatar;
    @JsonProperty("avatarmedium")
    private String avatarmedium;
    @JsonProperty("avatarfull")
    private String avatarfull;
    @JsonProperty("avatarhash")
    private String avatarhash;
    @JsonProperty("personastate")
    private Integer personastate;
    @JsonProperty("realname")
    private String realname;
    @JsonProperty("primaryclanid")
    private String primaryclanid;
    @JsonProperty("timecreated")
    private Integer timecreated;
    @JsonProperty("personastateflags")
    private Integer personastateflags;
    @JsonProperty("gameextrainfo")
    private String gameextrainfo;
    @JsonProperty("gameid")
    private String gameid;
    @JsonProperty("lobbysteamid")
    private String lobbysteamid;
    @JsonProperty("loccountrycode")
    private String loccountrycode;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("steamid")
    public String getSteamid() {
        return steamid;
    }

    @JsonProperty("steamid")
    public void setSteamid(String steamid) {
        this.steamid = steamid;
    }

    @JsonProperty("communityvisibilitystate")
    public Integer getCommunityvisibilitystate() {
        return communityvisibilitystate;
    }

    @JsonProperty("communityvisibilitystate")
    public void setCommunityvisibilitystate(Integer communityvisibilitystate) {
        this.communityvisibilitystate = communityvisibilitystate;
    }

    @JsonProperty("profilestate")
    public Integer getProfilestate() {
        return profilestate;
    }

    @JsonProperty("profilestate")
    public void setProfilestate(Integer profilestate) {
        this.profilestate = profilestate;
    }

    @JsonProperty("personaname")
    public String getPersonaname() {
        return personaname;
    }

    @JsonProperty("personaname")
    public void setPersonaname(String personaname) {
        this.personaname = personaname;
    }

    @JsonProperty("commentpermission")
    public Integer getCommentpermission() {
        return commentpermission;
    }

    @JsonProperty("commentpermission")
    public void setCommentpermission(Integer commentpermission) {
        this.commentpermission = commentpermission;
    }

    @JsonProperty("profileurl")
    public String getProfileurl() {
        return profileurl;
    }

    @JsonProperty("profileurl")
    public void setProfileurl(String profileurl) {
        this.profileurl = profileurl;
    }

    @JsonProperty("avatar")
    public String getAvatar() {
        return avatar;
    }

    @JsonProperty("avatar")
    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @JsonProperty("avatarmedium")
    public String getAvatarmedium() {
        return avatarmedium;
    }

    @JsonProperty("avatarmedium")
    public void setAvatarmedium(String avatarmedium) {
        this.avatarmedium = avatarmedium;
    }

    @JsonProperty("avatarfull")
    public String getAvatarfull() {
        return avatarfull;
    }

    @JsonProperty("avatarfull")
    public void setAvatarfull(String avatarfull) {
        this.avatarfull = avatarfull;
    }

    @JsonProperty("avatarhash")
    public String getAvatarhash() {
        return avatarhash;
    }

    @JsonProperty("avatarhash")
    public void setAvatarhash(String avatarhash) {
        this.avatarhash = avatarhash;
    }

    @JsonProperty("personastate")
    public Integer getPersonastate() {
        return personastate;
    }

    @JsonProperty("personastate")
    public void setPersonastate(Integer personastate) {
        this.personastate = personastate;
    }

    @JsonProperty("realname")
    public String getRealname() {
        return realname;
    }

    @JsonProperty("realname")
    public void setRealname(String realname) {
        this.realname = realname;
    }

    @JsonProperty("primaryclanid")
    public String getPrimaryclanid() {
        return primaryclanid;
    }

    @JsonProperty("primaryclanid")
    public void setPrimaryclanid(String primaryclanid) {
        this.primaryclanid = primaryclanid;
    }

    @JsonProperty("timecreated")
    public Integer getTimecreated() {
        return timecreated;
    }

    @JsonProperty("timecreated")
    public void setTimecreated(Integer timecreated) {
        this.timecreated = timecreated;
    }

    @JsonProperty("personastateflags")
    public Integer getPersonastateflags() {
        return personastateflags;
    }

    @JsonProperty("personastateflags")
    public void setPersonastateflags(Integer personastateflags) {
        this.personastateflags = personastateflags;
    }

    @JsonProperty("gameextrainfo")
    public String getGameextrainfo() {
        return gameextrainfo;
    }

    @JsonProperty("gameextrainfo")
    public void setGameextrainfo(String gameextrainfo) {
        this.gameextrainfo = gameextrainfo;
    }

    @JsonProperty("gameid")
    public String getGameid() {
        return gameid;
    }

    @JsonProperty("gameid")
    public void setGameid(String gameid) {
        this.gameid = gameid;
    }

    @JsonProperty("lobbysteamid")
    public String getLobbysteamid() {
        return lobbysteamid;
    }

    @JsonProperty("lobbysteamid")
    public void setLobbysteamid(String lobbysteamid) {
        this.lobbysteamid = lobbysteamid;
    }

    @JsonProperty("loccountrycode")
    public String getLoccountrycode() {
        return loccountrycode;
    }

    @JsonProperty("loccountrycode")
    public void setLoccountrycode(String loccountrycode) {
        this.loccountrycode = loccountrycode;
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
