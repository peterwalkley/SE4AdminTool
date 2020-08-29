package tfa.se4.steam;

import org.slf4j.Logger;

import tfa.se4.steam.json.JSONUtils;
import tfa.se4.steam.json.PlayerBansQueryResult;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.fluent.Request;

public final class SteamAPI
{
    private String m_steamAPIkey;
    
    public SteamAPI(final String apiKey)
    {
        m_steamAPIkey = apiKey;
    }
    
    /**
     * Query steam to get the info we're interested in.
     * @param steamID steam ID
     * @return state
     * @throws ClientProtocolException On error
     * @throws IOException On error
     */
    private PlayerBansQueryResult getPlayerBans(final String steamID, final Logger logger) throws ClientProtocolException, IOException
    {
        final String urltemplate = "http://api.steampowered.com/ISteamUser/GetPlayerBans/v1/?key=##STEAMAPIKEY##&steamids=##STEAMID##";
        
        final String url = urltemplate
                .replace("##STEAMID##", steamID)
                .replace("##STEAMAPIKEY##", m_steamAPIkey);
        
        return JSONUtils.unMarshalServerStatus(Request.Get(url).execute().returnContent().asString(), logger);
    }
    
    /**
     * Check for VAC ban.
     * @param steamID Steam ID
     * @param logger logger
     * @return true/false
     */
    public boolean isHasVACBan(final String steamID, final Logger logger)
    {
        logger.info("STEAM|{}|checking for VAC ban", steamID);
        try
        {
            final PlayerBansQueryResult result = getPlayerBans(steamID, logger);
            return result.getPlayers().get(0).getVACBanned();
        }
        catch (final IOException e)
        {
            logger.error("STEAM|{}|VAC ban check failed. Assuming player NOT banned", e);
            return false;
        }
    }
/* NOT USED, but kept handy
    public boolean isHasGameBan(final String steamID, final Logger logger)
    {
        logger.debug("STEAM|{}|checking for game ban", steamID);
        try
        {
            final PlayerBansQueryResult result = getPlayerBans(steamID, logger);
            return result.getPlayers().get(0).getNumberOfGameBans() > 0;
        }
        catch (final IOException e)
        {
            logger.error("STEAM|{}|GAME ban check failed. Assuming player NOT banned", e);
            return false;
        }
    }*/
}
