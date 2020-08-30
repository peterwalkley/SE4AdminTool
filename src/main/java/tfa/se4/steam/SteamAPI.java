package tfa.se4.steam;

import org.slf4j.Logger;

import tfa.se4.steam.json.JSONUtils;
import tfa.se4.steam.json.Player;
import tfa.se4.steam.json.PlayerBansQueryResult;

import java.io.IOException;

import org.apache.http.client.fluent.Request;

/**
 * Holder class for anything we use to talk to Steam.
 */
public final class SteamAPI
{
    private String m_steamAPIkey;
    
    /**
     * Initialise and remember steam API key.
     * @param apiKey Steam API key.
     */
    public SteamAPI(final String apiKey)
    {
        m_steamAPIkey = apiKey;
    }
    
    /**
     * Get player ban information.
     * @param steamID SteamId of player to query
     * @param logger Logger for catching errors
     * @return Ban information or null if we were not able to fetch it.
     */
    public Player getBanInfo(final String steamID, final Logger logger)
    {
        final String urltemplate = "http://api.steampowered.com/ISteamUser/GetPlayerBans/v1/?key=##STEAMAPIKEY##&steamids=##STEAMID##";
        
        final String url = urltemplate
                .replace("##STEAMID##", steamID)
                .replace("##STEAMAPIKEY##", m_steamAPIkey);
        
        try
        {
            final PlayerBansQueryResult bans = JSONUtils.unMarshalServerStatus(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);
            return bans.getPlayers().get(0);
        }
        catch (IOException e)
        {
            logger.error("Unable to fetch steam ban info for " + steamID, e);
            return null;
        }
    }
}
