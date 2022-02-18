package tfa.se4.steam;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.fluent.Request;

import tfa.se4.steam.json.JSONUtils;
import tfa.se4.steam.json.PlayerBanInfo;
import tfa.se4.steam.json.PlayerBansQueryResult;
import tfa.se4.steam.json.PlayerSummaryInfo;
import tfa.se4.steam.json.PlayerSummaryQueryResult;
import tfa.se4.steam.json.PlayingSharedGameResponse;

import tfa.se4.logger.LoggerInterface;
import tfa.se4.steam.json.RecentlyPlayedGame;
import tfa.se4.steam.json.RecentlyPlayedGamesResponse;

import static tfa.se4.logger.LoggerInterface.LogLevel;
import static tfa.se4.logger.LoggerInterface.LogType;

/**
 * Holder class for anything we do to talk to Steam.
 */
public final class SteamAPI
{
    /** Substitution string. */
    private static final String STEAMID = "##STEAMID##";

    /** Substitution string. */
    public static final String STEAMAPIKEY = "##STEAMAPIKEY##";

    /** Steam API key. */
    private final String mSteamAPIkey;

    /** Sniper Elite 4 steam APP ID. */
    private static final String APP_ID = "312660";

    /** Lookup cache to reduce hits to steam. */
    private static final Map<String, RecentlyPlayedGamesResponse> sRecentlyPlayedCache = new HashMap<>();

    /**
     * Initialise and remember steam API key.
     *
     * @param apiKey Steam API key.
     */
    public SteamAPI(final String apiKey)
    {
        mSteamAPIkey = apiKey;
    }

    /**
     * Get player ban information.
     * @see "https://partner.steamgames.com/doc/webapi/ISteamUser#GetPlayerBans"
     *
     * @param steamID SteamId of player to query
     * @param logger  Logger for catching errors
     * @return Ban information or null if we were not able to fetch it.
     */
    public PlayerBanInfo getBanInfo(final String steamID, final String playerName, final LoggerInterface logger)
    {
        logger.log(LogLevel.DEBUG, LogType.STEAM, "Checking player %s steam ID %s for bans", playerName, steamID);
        final String urlTemplate = "http://api.steampowered.com/ISteamUser/GetPlayerBans/v1/?key=##STEAMAPIKEY##&steamids=##STEAMID##";

        final String url = urlTemplate
                .replace(STEAMID, steamID)
                .replace(STEAMAPIKEY, mSteamAPIkey);

        try
        {
            final PlayerBansQueryResult bans = JSONUtils.unMarshalServerStatus(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            if (bans != null && bans.getPlayers() != null && !bans.getPlayers().isEmpty())
            {
                return bans.getPlayers().get(0);
            }

            logger.log(LogLevel.ERROR, LogType.STEAM, "Unable to fetch steam ban info for %s due to empty return data", steamID);
        }
        catch (IOException e)
        {
            logger.log(LogLevel.ERROR, LogType.STEAM, e, "Unable to fetch steam ban info for %s", steamID);
        }
        return null;
    }

    /**
     * Get player profile information.
     * @see "https://partner.steamgames.com/doc/webapi/ISteamUser#GetPlayerSummaries"
     *
     * @param steamID SteamId of player to query
     * @param logger  Logger for catching errors
     * @return Ban information or null if we were not able to fetch it.
     */
    public PlayerSummaryInfo getProfileInfo(final String steamID, final String playerName, final LoggerInterface logger)
    {
        logger.log(LogLevel.DEBUG, LogType.STEAM, "Checking player %s steam ID %s for profile visibility", playerName, steamID);
        final String urlTemplate = "http://api.steampowered.com/ISteamUser/GetPlayerSummaries/v2/?key=##STEAMAPIKEY##&steamids=##STEAMID##";

        final String url = urlTemplate
                .replace(STEAMID, steamID)
                .replace(STEAMAPIKEY, mSteamAPIkey);

        try
        {
            final PlayerSummaryQueryResult info = JSONUtils.unMarshalPlayerSummary(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            if (info != null && info.getResponse() != null && info.getResponse().getPlayers() != null && !info.getResponse().getPlayers().isEmpty())
            {
                return info.getResponse().getPlayers().get(0);
            }

            logger.log(LogLevel.ERROR, LogType.STEAM, "Unable to fetch steam player summary info for %s due to empty return data", steamID);
        }
        catch (IOException e)
        {
            logger.log(LogLevel.ERROR, LogType.STEAM, e, "Unable to fetch steam player summary info for %s", steamID);
        }
        return null;
    }

    /**
     * Get steamID of any linked profile we can find.
     * @see "https://partner.steamgames.com/doc/webapi/IPlayerService#IsPlayingSharedGame"
     *
     * @param steamID SteamId of player to query
     * @param logger  Logger for catching errors
     * @return SteamID of any linked profile we can find or null when none present.
     */
    public String getLinkedProfileID(final String steamID, final String playerName, final LoggerInterface logger)
    {
        logger.log(LogLevel.DEBUG, LogType.STEAM, "Checking player %s steam ID %s for linked profile", playerName, steamID);
        final String urlTemplate = "https://api.steampowered.com/IPlayerService/IsPlayingSharedGame/v1/?key=##STEAMAPIKEY##&steamid=##STEAMID##&appid_playing=##APPID##";

        final String url = urlTemplate
                .replace("##APPID##", APP_ID)
                .replace(STEAMID, steamID)
                .replace(STEAMAPIKEY, mSteamAPIkey);

        try
        {
            final PlayingSharedGameResponse info = JSONUtils.unMarshalPlayingSharedGame(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            if (info == null || info.getResponse() == null)
            {
                logger.log(LogLevel.ERROR, LogType.STEAM, "Unable to fetch linked profile info for %s due to empty return data", steamID);
                return null;
            }
            final String linkedSteamID = info.getResponse().getLenderSteamid();
            if (linkedSteamID == null || "0".equals(linkedSteamID))
            {
                return null;
            }

            return linkedSteamID;
        }
        catch (IOException e)
        {
            logger.log(LogLevel.ERROR, LogType.STEAM, e, "Unable to fetch linked profile for %s", steamID);
        }
        return null;
    }

    /**
     * Get total playtime (in hours) for SE 4.
     * @see "https://partner.steamgames.com/doc/webapi/IPlayerService#GetRecentlyPlayedGames"
     * @param steamID SteamId of player to query
     * @param logger  Logger for catching errors
     * @return playtime in hours or null if we can't get the information.
     */
    public synchronized String getTotalPlaytimeHours(final String steamID, final String playerName, final LoggerInterface logger)
    {
        if (sRecentlyPlayedCache.containsKey(steamID))
        {
            return getHours(sRecentlyPlayedCache.get(steamID));
        }

        logger.log(LogLevel.DEBUG, LogType.STEAM, "Checking player %s steam ID %s for total play time", playerName, steamID);
        final String urlTemplate = "http://api.steampowered.com/IPlayerService/GetRecentlyPlayedGames/v1/?key=##STEAMAPIKEY##&steamid=##STEAMID##";

        final String url = urlTemplate
                .replace(STEAMID, steamID)
                .replace(STEAMAPIKEY, mSteamAPIkey);

        try
        {
            final RecentlyPlayedGamesResponse info = JSONUtils.unMarshalRecentlyPlayedGamesResponse(
                    Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString(), logger);

            sRecentlyPlayedCache.put(steamID, info);
            final String hours = getHours(info);
            logger.log(LogLevel.INFO, LogType.STEAM, "Player %s steam ID %s has %s hours total play time", playerName, steamID, hours);
            return hours;
        }
        catch (IOException e)
        {
            logger.log(LogLevel.ERROR, LogType.STEAM, e, "Unable to fetch recently played games info for %s", steamID);
        }
        return "-";
    }

    /**
     * Get hours play time or null if not present.
     * @param info RecentlyPlayedGamesResponse
     * @return hours or '-' for none
     */
    private String getHours(final RecentlyPlayedGamesResponse info)
    {
        if (info == null || info.getResponse() == null || info.getResponse().getGames() == null)
        {
            return "-";
        }

        for (final RecentlyPlayedGame g : info.getResponse().getGames())
        {
            if (APP_ID.equals(g.getAppid().toString()))
            {
                int minutes = g.getPlaytimeForever();
                return Integer.toString(minutes / 60);
            }
        }

        return "-";
    }
}
