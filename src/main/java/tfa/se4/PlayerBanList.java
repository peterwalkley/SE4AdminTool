package tfa.se4;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import tfa.se4.logger.LoggerInterface;

import static tfa.se4.logger.LoggerInterface.LogLevel;
import static tfa.se4.logger.LoggerInterface.LogType;

/**
 * Read list of banned player steam ID and reasons.  Format is one
 * player ID, name and reason per line.
 * <p>
 * Duplicates will automatically be ignored.
 * <p>
 * A banned player is automatically kicked.
 * <p>
 * The file will be checked for updates every 30 seconds and
 * re-read if it has changed.
 */
public final class PlayerBanList implements Runnable
{
    public static class Ban
    {
        public String steamID; //NOSONAR This is a simple bean holder
        public String name; //NOSONAR This is a simple bean holder
        public String reason; //NOSONAR This is a simple bean holder
    }

    private Map<String, Ban> mBans = new HashMap<>();
    private final File mFile;
    private long mLastRead = 0;
    private final LoggerInterface mLogger;

    public PlayerBanList(final String fileName, final LoggerInterface logger)
    {
        mFile = new File(fileName);
        mLogger = logger;
        new Thread(this).start();
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                if (FileUtils.isFileNewer(mFile, mLastRead))
                {
                    final Map<String, Ban> bans = new HashMap<>();
                    final List<String> lines = FileUtils.readLines(mFile, Charset.defaultCharset());
                    for (final String line : lines)
                    {
                        if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#"))
                        {
                            final String[] terms = StringUtils.split(line, '#');
                            if (terms.length == 3)
                            {
                                final Ban ban = new Ban();
                                ban.steamID = terms[0].trim();
                                ban.name = terms[1].trim();
                                ban.reason = StringUtils.isBlank(terms[2]) ? "Unspecified" : terms[2].trim();
                                bans.put(ban.steamID, ban);
                            }
                            else
                            {
                                mLogger.log(LogLevel.INFO, LogType.SYSTEM, "Invalid ban entry '%s' read from %s was ignored", line, mFile.getName());
                            }
                        }

                    }
                    mBans = bans;
                    mLogger.log(LogLevel.INFO, LogType.SYSTEM, "%s player bans read from %s", Integer.toString(mBans.size()), mFile.getName());
                    mLastRead = System.currentTimeMillis();
                }

                Utils.sleep(30000);
            }
        }
        catch (final IOException ex)
        {
            mLogger.log(LogLevel.ERROR, LogType.SYSTEM, ex, "Cannot read player bans from %s. Error is %s", mFile.getName(), ex.getMessage());
        }
    }

    /**
     * Get the ban information.
     *
     * @param steamID steam ID to check
     * @return true/false.
     */
    public Ban getBan(final String steamID)
    {
        return mBans.get(steamID);
    }

    /**
     * Add a ban to ban list.
     *
     * @param steamID    Steam ID
     * @param playerName Player name
     * @param reason     Reason for the ban
     * @return true if ban added, false if was already present
     */
    public boolean addBan(final String steamID, final String playerName, final String reason)
    {
        if (getBan(steamID) == null)
        {
            final String banText = steamID + " # " + playerName.replace('#', 'x') + " # " + reason;

            try
            {
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(mFile.getAbsolutePath(), true)));
                out.println(banText);
                out.close();
            }
            catch (IOException e)
            {
                // Ignore as we can't do much
                System.out.println("Error adding banned player to ban list");
                e.printStackTrace();
            }
            return true;
        }

        return false;
    }
}
