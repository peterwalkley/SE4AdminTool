package tfa.se4.iplookup.extremeip;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tfa.se4.iplookup.ipstack.json.IPStack;
import tfa.se4.logger.LoggerInterface;

public final class JSONUtils
{
    /**
     * Object mapper.
     */

    private static final ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Utility class private.
     */
    private JSONUtils()
    {
        // do nothing
    }

    /**
     * Convert supplied JSON string to IPStack result structure.
     *
     * @param json   JSON from IP stack.
     * @param logger Logger for writing error information.
     * @return Object or null when fails to unmarshal.
     */
    public static ExtremeIPResponse unMarshalServerStatus(final String json, final LoggerInterface logger)
    {
        try
        {
            return MAPPER.readValue(json, ExtremeIPResponse.class);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LoggerInterface.LogLevel.ERROR, LoggerInterface.LogType.SYSTEM, e, "Bad JSON %s", json);
            return null;
        }
    }
}
