package tfa.se4;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Options
{
	private String m_hostname;
	private String m_port;
	private String m_password;
	private String m_ipBansFile;
	private String m_whiteListFile;
	private String m_bansListFile;
	private String m_playerGreeting;
	
	/**
	 * Collect properties.
	 * @param configFile
	 */
	public Options(final String configFile) throws Exception
	{
		final Properties props = new Properties();
		try
		{
			props.load(new FileInputStream(configFile));
		}
		catch (final IOException e)
		{
			System.err.print("Unable to read configuration properties file " + configFile);
			throw e;
		}
		
		m_hostname = getMandatoryProperty(props, "server.host");
		m_port = getMandatoryProperty(props, "server.port");
		m_password = getMandatoryProperty(props, "server.password");
		
		m_playerGreeting = props.getProperty("player.greeting", "").trim();
		m_ipBansFile = getMandatoryFile(props, "banned.ip");
		m_bansListFile = getMandatoryFile(props, "banned.players");
		m_whiteListFile = getMandatoryFile(props, "whitelist.players");
	}
	
	/**
	 * Get mandatory property.
	 * @param props Properties contents
	 * @param prop property to get
	 * @return Value
	 * @throws Exception When value is missing
	 */
	private String getMandatoryProperty(final Properties props, final String prop) throws Exception
	{
		final String val = props.getProperty(prop);
		if (StringUtils.isBlank(val))
			throw new IllegalArgumentException("Property " + prop + " cannot be a blank value");
		
		return val.trim();
	}
	
	/**
	 * Get mandatory file.
	 * @param props Properties contents
	 * @param prop property to get
	 * @return Value
	 * @throws Exception When value is missing or is not a file
	 */
	private String getMandatoryFile(final Properties props, final String prop) throws Exception
	{
		final String val = getMandatoryProperty(props, prop);
		if (!(new File(val)).isFile())
			throw new IllegalArgumentException("Property " + prop + " does not refer to a file");
		
		return val;
	}
	
	/**
	 * Get hostname.
	 * @return Host name
	 */
	public String getHost()
	{
		return m_hostname;
	}
	
	/**
	 * Get port.
	 * @return port
	 */
	public String getPort()
	{
		return m_port;
	}
	
	/**
	 * Get password.
	 * @return password
	 */
	public String getPassword()
	{
		return m_password;
	}
	
	/**
	 * Get IP address ban file location.
	 * @return IP address ban file location
	 */
	public String getIPBansFile()
	{
		return m_ipBansFile;
	}
	
	/**
	 * Get Player whitelist file location.
	 * @return Player whitelist file location
	 */
	public String getWhiteListFile()
	{
		return m_whiteListFile;
	}
	
	/**
	 * Get Player bans file location.
	 * @return Player bans file location
	 */
	public String getPlayerBansListFile()
	{
		return m_bansListFile;
	}
	
	/**
	 * Get Player greeting message.
	 * @return Player greeting message
	 */
	public String getPlayerGreeting()
	{
		return m_playerGreeting;
	}
}
