package tfa.se4;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import tfa.se4.logger.LoggerInterface;

import static tfa.se4.logger.LoggerInterface.LogLevel;
import static tfa.se4.logger.LoggerInterface.LogType;

/**
 * Read list of white listed player steam ID's.  Format is one
 * player ID per line. Duplicates will automatically be ignored.
 * <p>
 * A white listed player will not be checked for VAC or Game bans.
 * The file will be checked for updates every 30 seconds and
 * re-read if it has changed.
 */
public final class PlayerWhiteList implements Runnable
{
    private HashSet<String> mIds = new HashSet<>();
    private final File mFile;
    private long mLastRead = 0;
    private final LoggerInterface mLogger;

    public PlayerWhiteList(final String fileName, final LoggerInterface logger)
    {
        mFile = new File(fileName);
        mLogger = logger;
        new Thread(this).start();
    }

    @SuppressWarnings({"InfiniteLoopStatement", "BusyWait"})
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                if (FileUtils.isFileNewer(mFile, mLastRead))
                {
                    final HashSet<String> ids = new HashSet<>();
                    final List<String> lines = FileUtils.readLines(mFile, Charset.defaultCharset());
                    for (final String line : lines)
                    {
                        if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#"))
                        {
                            ids.add(StringUtils.split(line, '#')[0].trim());
                        }
                    }

                    mIds = ids;
                    mLogger.log(LogLevel.INFO, LogType.SYSTEM, "%d whitelisted steam IDs read from %s", mIds.size(), mFile.getName());
                    mLastRead = System.currentTimeMillis();
                }
                Utils.sleep(30000);
            }
        }
        catch (final IOException ex)
        {
            mLogger.log(LogLevel.ERROR, LogType.SYSTEM, ex, "Cannot read player whitelist from %s. Error is %s", mFile.getName(), ex.getMessage());
        }
    }

    /**
     * Is the steam ID white listed
     *
     * @param steamID steam ID to check
     * @return true/false.
     */
    public boolean isWhitelisted(final String steamID)
    {
        return mIds.contains(steamID);
    }
}
