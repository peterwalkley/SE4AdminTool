package tfa.se4;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import tfa.se4.logger.LoggerInterface;

import static tfa.se4.logger.LoggerInterface.LogLevel;
import static tfa.se4.logger.LoggerInterface.LogType;

/**
 * Read list of reasons why we might kick or ban someone.
 * <p>
 * The file will be checked for updates every 30 seconds and
 * re-read if it has changed.
 */
public final class KickBanReasons implements Runnable
{
    private List<String> mReasons = new ArrayList<>();
    private long mLastRead = 0;
    private final LoggerInterface mLogger;
    private final File mFile;

    /**
     * Use 'kick_ban_reasons.txt' reasons file located in same directory as
     * fileName.
     *
     * @param logger Logger for messages.
     */
    public KickBanReasons(final LoggerInterface logger)
    {
        final String filename = "kick_ban_reasons.txt";
        FileHelper.cloneConfigFileFromExampleIfMissing(filename);
        mFile = FileHelper.getConfigFile(filename);

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
                    final List<String> reasons = new ArrayList<>();
                    final List<String> lines = FileUtils.readLines(mFile, Charset.defaultCharset());
                    for (final String line : lines)
                    {
                        if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#"))
                        {
                            reasons.add(StringUtils.split(line, '#')[0].trim());
                        }
                    }
                    mReasons = reasons;
                    mLogger.log(LogLevel.INFO, LogType.SYSTEM, "%s kick/ban reasons read from %s", Integer.toString(mReasons.size()), mFile.getName());
                    mLastRead = System.currentTimeMillis();
                }
                Utils.sleep(30000);
            }
        }
        catch (final IOException ex)
        {
            mLogger.log(LogLevel.ERROR, LogType.SYSTEM, ex, "Cannot read kick/ban reasons from %s. Error is %s", mFile.getName(), ex.getMessage());
        }
    }

    /**
     * Hard-coded reasons if we can't read reasons file.
     */
    private static final List<String> S_DEFAULTS = new ArrayList<>(Arrays.asList(
            "unspecified", "shoulder aim", "spawn camping", "failure to listen to admin"
    ));

    /**
     * Get the reasons list.
     *
     * @return list of reasons.
     */
    public List<String> getReasons()
    {
        if (mReasons == null || mReasons.isEmpty())
        {
            return S_DEFAULTS;
        }
        return mReasons;
    }
}
