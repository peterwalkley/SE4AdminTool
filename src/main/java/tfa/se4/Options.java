package tfa.se4;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class Options
{
    public static final int CLOSED_PROFILE_IGNORE = 0;
    public static final int CLOSED_PROFILE_WARN = 1;
    public static final int CLOSED_PROFILE_KICK = 2;

    private final String m_hostname;
    private final String m_port;
    private final String m_password;
    private final String m_ipBansFile;
    private final String m_whiteListFile;
    private final String m_bansListFile;
    private final String m_playerGreeting;
    private final String m_steamAPIKey;
    private final String m_ipStackAPIKey;
    private boolean m_applyVACBans;
    private boolean m_applyGameBans;
    private int m_closedProfilePolicy;
    private final String m_closedProfileMessage;

    /**
     * Collect properties.
     *
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
        m_closedProfilePolicy = getOptionalInt(props, "closed.profile.policy", 0, 0, 2);
        m_closedProfileMessage = props.getProperty("closed.profile.message", "Hello #PlayerName#, please open your steam profile to public.").trim();
    }

    /**
     * Check if the settings are inconsistent, i.e. trying to use features that need a steam API key.
     * @return true/false if we
     */
    public boolean hasInvalidSteamSettings()
    {
        return (m_applyVACBans || m_applyGameBans || m_closedProfilePolicy != CLOSED_PROFILE_IGNORE) && StringUtils.isBlank(m_steamAPIKey);
    }

    /**
     * Change settings so that we won't try and use things that require a steam API key.
     */
    public void makeSteamSettingsConsistent()
    {
        m_applyVACBans = false;
        m_applyGameBans = false;
        m_closedProfilePolicy = CLOSED_PROFILE_IGNORE;
    }

    /**
     * Get mandatory property.
     *
     * @param props Properties contents
     * @param prop  property to get
     * @return Value
     * @throws Exception When value is missing
     */
    private String getMandatoryProperty(final Properties props, final String prop) throws Exception
    {
        final String val = props.getProperty(prop);
		if (StringUtils.isBlank(val))
		{
			throw new Exception("Property " + prop + " cannot be a blank value");
		}

        return val.trim();
    }

    /**
     * Get optional int property.
     * @param props Properties contents
     * @param prop  property to get
     * @param defaultValue Default to return if nothing set or something invalid is set.
     * @param low Lowest acceptable value (inclusive)
     * @param high Highest acceptable value (inclusive)
     * @return value
     */
    private int getOptionalInt(final Properties props, final String prop, final int defaultValue, final int low, final int high)
    {
        final String val = props.getProperty(prop);
        if (StringUtils.isBlank(val))
        {
            return defaultValue;
        }

        try
        {
            int i = Integer.parseInt(val);
            return (i >= low && i <= high) ? i : defaultValue;
        }
        catch (final NumberFormatException ex)
        {
            return defaultValue;
        }
    }

    /**
     * Get mandatory file.
     *
     * @param props Properties contents
     * @param prop  property to get
     * @return Value
     * @throws Exception When value is missing or is not a file
     */
    private String getMandatoryFile(final Properties props, final String prop) throws Exception
    {
        final String val = getMandatoryProperty(props, prop);
		if (!(new File(val)).isFile())
		{
			throw new Exception("Property " + prop + " does not refer to a file");
		}

        return val;
    }

    /**
     * Get hostname.
     *
     * @return Host name
     */
    public String getHost()
    {
        return m_hostname;
    }

    /**
     * Get port.
     *
     * @return port
     */
    public String getPort()
    {
        return m_port;
    }

    /**
     * Get password.
     *
     * @return password
     */
    public String getPassword()
    {
        return m_password;
    }

    /**
     * Get IP address ban file location.
     *
     * @return IP address ban file location
     */
    public String getIPBansFile()
    {
        return m_ipBansFile;
    }

    /**
     * Get Player whitelist file location.
     *
     * @return Player whitelist file location
     */
    public String getWhiteListFile()
    {
        return m_whiteListFile;
    }

    /**
     * Get Player bans file location.
     *
     * @return Player bans file location
     */
    public String getPlayerBansListFile()
    {
        return m_bansListFile;
    }

    /**
     * Get Player greeting message.
     *
     * @return Player greeting message
     */
    public String getPlayerGreeting()
    {
        return m_playerGreeting;
    }

    /**
     * Whether to apply VAC bans.
     *
     * @return true/false
     */
    public boolean isApplyVACBans()
    {
        return m_applyVACBans;
    }

    /**
     * Whether to apply game bans.
     *
     * @return true/false
     */
    public boolean isApplyGameBans()
    {
        return m_applyGameBans;
    }

    /**
     * Get Steam API key.
     *
     * @return Key
     */
    public String getSteamAPIKey()
    {
        return m_steamAPIKey;
    }

    /**
     * Get IPStack API key.
     *
     * @return Key
     */
    public String getIPStackAPIKey()
    {
        return m_ipStackAPIKey;
    }

    /**
     * Get closed profile message.
     *
     * @return message
     */
    public String getClosedProfileMessage()
    {
        return m_closedProfileMessage;
    }

    /**
     * Get closed profile policy.
     * @return policy
     */
    public int getClosedProfilePolicy()
    {
        return m_closedProfilePolicy;
    }
}
