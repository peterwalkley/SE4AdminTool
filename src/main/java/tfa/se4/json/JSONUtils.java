package tfa.se4.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tfa.se4.logger.LoggerInterface;

import java.util.Locale;

import static tfa.se4.logger.LoggerInterface.LogLevel;
import static tfa.se4.logger.LoggerInterface.LogType;

public final class JSONUtils
{
    /**
     * Object mapper.
     */
    private static ObjectMapper sMapper = null;

    /**
     * Utility class private.
     */
    private JSONUtils()
    {
        // do nothing
    }

    /**
     * Get mapper.
     *
     * @return mapper
     */
    private static synchronized ObjectMapper getMapper()
    {
        if (sMapper == null)
        {
            sMapper = new ObjectMapper();
            sMapper.setLocale(Locale.US);
        }
        return sMapper;
    }

    /**
     * Convert supplied JSON string to ServerStatus structure.
     *
     * @param json   JSON from SE server.
     * @param logger Logger for writing error information.
     * @return Object or null when fails to unmarshal.
     */
    public static ServerStatus unMarshalServerStatus(final String json, final LoggerInterface logger)
    {
        try
        {
            return getMapper().readValue(json, ServerStatus.class);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LogLevel.ERROR, LogType.SYSTEM, e, "Bad JSON %s", json);
            return null;
        }
    }

    /**
     * Marshal the player object structure back to JSON.
     *
     * @param p player
     * @param logger Logger for writing error information.
     * @return Marshalled string or empty when marshalling error.
     */
    public static String marshalPlayer(final Player p, final LoggerInterface logger)
    {
        try
        {
            return getMapper().writeValueAsString(p);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LogLevel.ERROR, LogType.SYSTEM, e, "Marshalling error");
            return "";
        }
    }

    /**
     * Marshal the ServerStatus object structure back to JSON.
     *
     * @param status ServerStatus
     * @param logger Logger for writing error information.
     * @return Marshalled string or empty when marshalling error.
     */
    public static String marshalServerStatus(final ServerStatus status, final LoggerInterface logger)
    {
        try
        {
            return getMapper().writeValueAsString(status);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LogLevel.ERROR, LogType.SYSTEM, e, "Marshalling error");
            return "";
        }
    }
}
