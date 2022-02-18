package tfa.se4.steam.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tfa.se4.logger.LoggerInterface;

import static tfa.se4.logger.LoggerInterface.LogLevel;
import static tfa.se4.logger.LoggerInterface.LogType;

public final class JSONUtils
{
    /** Object mapper. */
    private static final ObjectMapper MAPPER = new ObjectMapper();

    /** Bad JSON message. */
    private static final String BAD_JSON_MSG = "Bad JSON %s";

    /**
     * Utility class private.
     */
    private JSONUtils()
    {
        // do nothing
    }

    /**
     * Convert supplied JSON string to PlayerBansQueryResult structure.
     *
     * @param json   JSON from steam.
     * @param logger Logger for writing error information.
     * @return Object or null when fails to unmarshal.
     */
    public static PlayerBansQueryResult unMarshalServerStatus(final String json, final LoggerInterface logger)
    {
        try
        {
            return MAPPER.readValue(json, PlayerBansQueryResult.class);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LogLevel.ERROR, LogType.SYSTEM, e, BAD_JSON_MSG, json);
            return null;
        }
    }

    /**
     * Convert supplied JSON string to PlayerSummaryQueryResult structure.
     *
     * @param json   JSON from steam.
     * @param logger Logger for writing error information.
     * @return Object or null when fails to unmarshal.
     */
    public static PlayerSummaryQueryResult unMarshalPlayerSummary(final String json, final LoggerInterface logger)
    {
        try
        {
            return MAPPER.readValue(json, PlayerSummaryQueryResult.class);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LogLevel.ERROR, LogType.SYSTEM, e, BAD_JSON_MSG, json);
            return null;
        }
    }

    /**
     * Convert supplied JSON string to PlayingSharedGameResponse structure.
     *
     * @param json   JSON from steam.
     * @param logger Logger for writing error information.
     * @return Object or null when fails to unmarshal.
     */
    public static PlayingSharedGameResponse unMarshalPlayingSharedGame(final String json, final LoggerInterface logger)
    {
        try
        {
            return MAPPER.readValue(json, PlayingSharedGameResponse.class);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LogLevel.ERROR, LogType.SYSTEM, e, BAD_JSON_MSG, json);
            return null;
        }
    }

    /**
     * Convert supplied JSON string to RecentlyPlayedGamesResponse structure.
     *
     * @param json   JSON from steam.
     * @param logger Logger for writing error information.
     * @return Object or null when fails to unmarshal.
     */
    public static RecentlyPlayedGamesResponse unMarshalRecentlyPlayedGamesResponse(final String json, final LoggerInterface logger)
    {
        try
        {
            return MAPPER.readValue(json, RecentlyPlayedGamesResponse.class);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LogLevel.ERROR, LogType.SYSTEM, e, BAD_JSON_MSG, json);
            return null;
        }
    }
}
