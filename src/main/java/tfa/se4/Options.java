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
	private String m_steamAPIKey;
	private String m_ipStackAPIKey;
	private boolean m_applyVACBans;
    private boolean m_applyGameBans;
    
	/**
	 * Collect properties.
	 * @param configFile Properties configuration
	 */
	public Options(final String configFile) throws Exception
	{
		final Properties props = new Properties();
		try (FileInputStream input = new FileInputStream(configFile))
		{
			props.load(input);
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
		
		m_applyVACBans = Boolean.parseBoolean(props.getProperty("kick.vac.banned", "false").trim());
		m_applyGameBans = Boolean.parseBoolean(props.getProperty("kick.game.banned", "false").trim());
		m_steamAPIKey = props.getProperty("steam.api.key", "").trim();
		m_ipStackAPIKey = props.getProperty("ipstack.api.key", "").trim();

		if ((m_applyVACBans || m_applyGameBans) && StringUtils.isBlank(m_steamAPIKey))
		{
		    throw new Exception("Kicking of VAC or Game banned player requires a steam API key for checks to be performed");
		}
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
			throw new Exception("Property " + prop + " cannot be a blank value");
		
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
			throw new Exception("Property " + prop + " does not refer to a file");
		
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
	
	/**
	 * Whether to apply VAC bans.
	 * @return true/false
	 */
	public boolean isApplyVACBans()
	{
	    return m_applyVACBans;
	}
	
    /**
     * Whether to apply game bans.
     * @return true/false
     */
    public boolean isApplyGameBans()
    {
        return m_applyGameBans;
    }
    
	/**
	 * Get Steam API key.
	 * @return Key
	 */
	public String getSteamAPIKey()
	{
	    return m_steamAPIKey;
	}

	/**
	 * Get IPStack API key.
	 * @return Key
	 */
	public String getIPStackAPIKey()
	{
		return m_ipStackAPIKey;
	}
}
