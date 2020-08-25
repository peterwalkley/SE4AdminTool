package tfa.se4.json;

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
	 * Convert supplied JSON string to ServerStatus structure.
	 * @param json JSON from SE server.
	 * @param logger Logger for writing error information.
	 * @return Object or null when fails to unmarshal.
	 */
	public static ServerStatus unMarshalServerStatus(final String json, final Logger logger)
	{
		try
		{
			return MAPPER.readValue(json, ServerStatus.class);
		}
		catch (JsonProcessingException e)
		{
        	logger.debug("MSG||Bad JSON {} Error was: {}", json, e.getMessage());
			return null;
		}
	}
	
	/**
	 * Marshal the ServerStatus object structure back to JSON.
	 * @param status ServerStatus
	 * @param logger Logger for writing error information.
	 * @return Marshalled string or empty when marshalling error.
	 */
	public static String marshalServerStatus(final ServerStatus status, final Logger logger)
	{
		try
		{
			return MAPPER.writeValueAsString(status);
		} catch (JsonProcessingException e)
		{
        	logger.debug("MSG||Marshal error was: {}", e.getMessage());
			return "";
		}
	}
}
