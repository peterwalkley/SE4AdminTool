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
    private List<String> m_reasons = new ArrayList<>();
    private long m_lastRead = 0;
    private LoggerInterface m_logger;
    private File m_file;

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
        m_file = FileHelper.getConfigFile(filename);

        m_logger = logger;
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
                if (FileUtils.isFileNewer(m_file, m_lastRead))
                {
                    final List<String> reasons = new ArrayList<>();
                    final List<String> lines = FileUtils.readLines(m_file, Charset.defaultCharset());
                    for (final String line : lines)
                    {
                        if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#"))
                        {
                            reasons.add(StringUtils.split(line, '#')[0].trim());
                        }
                    }
                    m_reasons = reasons;
                    m_logger.log(LogLevel.INFO, LogType.SYSTEM, "%s kick/ban reasons read from %s", Integer.toString(m_reasons.size()), m_file.getName());
                    m_lastRead = System.currentTimeMillis();
                }
                Thread.sleep(30000);
            }
        }
        catch (final IOException | InterruptedException ex)
        {
            m_logger.log(LogLevel.ERROR, LogType.SYSTEM, ex, "Cannot read kick/ban reasons from %s. Error is %s", m_file.getName(), ex.getMessage());
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
        if (m_reasons == null || m_reasons.isEmpty())
        {
            return S_DEFAULTS;
        }
        return m_reasons;
    }
}
