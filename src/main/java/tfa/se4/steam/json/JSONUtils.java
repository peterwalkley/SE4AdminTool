package tfa.se4.steam.json;

import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class JSONUtils
{
    /** Object mapper. */
    
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    /** Utility class private. */
    private JSONUtils()
    {
        // do nothing
    }
    
    /**
     * Convert supplied JSON string to VACQueryResult structure.
     * @param json JSON from SE server.
     * @param logger Logger for writing error information.
     * @return Object or null when fails to unmarshal.
     */
    public static PlayerBansQueryResult unMarshalServerStatus(final String json, final Logger logger)
    {
        try
        {
            return MAPPER.readValue(json, PlayerBansQueryResult.class);
        }
        catch (JsonProcessingException e)
        {
            logger.debug("MSG||Bad JSON {} Error was: {}", json, e.getMessage());
            return null;
        }
    }
}
