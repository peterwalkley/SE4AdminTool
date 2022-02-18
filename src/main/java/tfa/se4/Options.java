package tfa.se4;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.*;
import org.apache.commons.lang3.StringUtils;

public class Options
{
    public static final int CLOSED_PROFILE_IGNORE = 0;
    public static final int CLOSED_PROFILE_WARN = 1;
    public static final int CLOSED_PROFILE_KICK = 2;

    private final String mHostname;
    private final String mPort;
    private final String mPassword;
    private final String mIPBansFile;
    private final String mWhiteListFile;
    private final String mBansListFile;
    private final String mPlayerGreeting;
    private final String mSteamAPIKey;
    private final String mExtremeIPAPIKey;
    private boolean mApplyVACBans;
    private boolean mApplyGameBans;
    private int mClosedProfilePolicy;
    private final String mClosedProfileMessage;
    private int mPlayHoursLimit;

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

        mHostname = getMandatoryProperty(props, "server.host");
        mPort = getMandatoryProperty(props, "server.port");
        mPassword = getMandatoryProperty(props, "server.password");

        mPlayerGreeting = props.getProperty("player.greeting", "").trim();
        mIPBansFile = getMandatoryFile(props, "banned.ip");
        mBansListFile = getMandatoryFile(props, "banned.players");
        mWhiteListFile = getMandatoryFile(props, "whitelist.players");

        mApplyVACBans = Boolean.parseBoolean(props.getProperty("kick.vac.banned", "false").trim());
        mApplyGameBans = Boolean.parseBoolean(props.getProperty("kick.game.banned", "false").trim());
        mSteamAPIKey = props.getProperty("steam.api.key", "").trim();
        mExtremeIPAPIKey = props.getProperty("extremeip.api.key", "").trim();
        mClosedProfilePolicy = getOptionalInt(props, "closed.profile.policy", 0, 0, 2);
        mClosedProfileMessage = props.getProperty("closed.profile.message", "Hello #PlayerName#, please open your steam profile to public.").trim();
        mPlayHoursLimit = getOptionalInt(props, "maximum.playhours", -1, 0, Integer.MAX_VALUE);
    }

    /**
     * Check if the settings are inconsistent, i.e. trying to use features that need a steam API key.
     * @return true/false if we are invalid
     */
    public boolean hasInvalidSteamSettings()
    {
        return (mApplyVACBans || mApplyGameBans || mClosedProfilePolicy != CLOSED_PROFILE_IGNORE || mPlayHoursLimit != -1) && StringUtils.isBlank(mSteamAPIKey);
    }

    /**
     * Change settings so that we won't try and use things that require a steam API key.
     */
    public void makeSteamSettingsConsistent()
    {
        mApplyVACBans = false;
        mApplyGameBans = false;
        mClosedProfilePolicy = CLOSED_PROFILE_IGNORE;
        mPlayHoursLimit = -1;
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
     */
    private String getMandatoryFile(final Properties props, final String prop) throws Exception
    {
        final String val = getMandatoryProperty(props, prop);
		if (!(new File(val)).isFile())
		{
		    // create empty one
            FileUtils.touch(new File(val));
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
        return mHostname;
    }

    /**
     * Get port.
     *
     * @return port
     */
    public String getPort()
    {
        return mPort;
    }

    /**
     * Get password.
     *
     * @return password
     */
    public String getPassword()
    {
        return mPassword;
    }

    /**
     * Get IP address ban file location.
     *
     * @return IP address ban file location
     */
    public String getIPBansFile()
    {
        return mIPBansFile;
    }

    /**
     * Get Player whitelist file location.
     *
     * @return Player whitelist file location
     */
    public String getWhiteListFile()
    {
        return mWhiteListFile;
    }

    /**
     * Get Player bans file location.
     *
     * @return Player bans file location
     */
    public String getPlayerBansListFile()
    {
        return mBansListFile;
    }

    /**
     * Get Player greeting message.
     *
     * @return Player greeting message
     */
    public String getPlayerGreeting()
    {
        return mPlayerGreeting;
    }

    /**
     * Whether to apply VAC bans.
     *
     * @return true/false
     */
    public boolean isApplyVACBans()
    {
        return mApplyVACBans;
    }

    /**
     * Whether to apply game bans.
     *
     * @return true/false
     */
    public boolean isApplyGameBans()
    {
        return mApplyGameBans;
    }

    /**
     * Get Steam API key.
     *
     * @return Key
     */
    public String getSteamAPIKey()
    {
        return mSteamAPIKey;
    }

    /**
     * Get Extreme IP API key.
     *
     * @return Key
     */
    public String getExtremeIPAPIKey()
    {
        return mExtremeIPAPIKey;
    }

    /**
     * Get closed profile message.
     *
     * @return message
     */
    public String getClosedProfileMessage()
    {
        return mClosedProfileMessage;
    }

    /**
     * Get closed profile policy.
     * @return policy
     */
    public int getClosedProfilePolicy()
    {
        return mClosedProfilePolicy;
    }

    /**
     * Get play hours limit.
     * @return Limit applied for play hours
     */
    public int getPlayHoursLimit()
    {
        return mPlayHoursLimit;
    }
}
