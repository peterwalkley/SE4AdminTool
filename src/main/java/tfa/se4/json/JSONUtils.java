package tfa.se4.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import tfa.se4.logger.LoggerInterface;
import static tfa.se4.logger.LoggerInterface.LogLevel;
import static tfa.se4.logger.LoggerInterface.LogType;

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
	public static ServerStatus unMarshalServerStatus(final String json, final LoggerInterface logger)
	{
		try
		{
			return MAPPER.readValue(json, ServerStatus.class);
		}
		catch (JsonProcessingException e)
		{
			logger.log(LogLevel.ERROR, LogType.SYSTEM, e, "Bad JSON %s", json);
			return null;
		}
	}
	
	/**
	 * Marshal the ServerStatus object structure back to JSON.
	 * @param status ServerStatus
	 * @param logger Logger for writing error information.
	 * @return Marshalled string or empty when marshalling error.
	 */
	public static String marshalServerStatus(final ServerStatus status, final LoggerInterface logger)
	{
		try
		{
			return MAPPER.writeValueAsString(status);
		} catch (JsonProcessingException e)
		{
			logger.log(LogLevel.ERROR, LogType.SYSTEM, e, "Marshalling error");
			return "";
		}
	}
}
