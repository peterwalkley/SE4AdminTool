package tfa.se4.iplookup.ipapi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import tfa.se4.logger.LoggerInterface;

public class JSONUtils {
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
     * Convert supplied JSON string to result structure.
     *
     * @param json   JSON from IP stack.
     * @param logger Logger for writing error information.
     * @return Object or null when fails to unmarshal.
     */
    public static IPAPIResponse unMarshalServerStatus(final String json, final LoggerInterface logger)
    {
        try
        {
            return MAPPER.readValue(json, IPAPIResponse.class);
        }
        catch (JsonProcessingException e)
        {
            logger.log(LoggerInterface.LogLevel.ERROR, LoggerInterface.LogType.SYSTEM, e, "Bad JSON %s", json);
            return null;
        }
    }
}
