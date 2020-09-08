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
 * Read list of banned IP addresses from file.  Format is one
 * IP address per line. Duplicates will automatically be ignored.
 * 
 * The file will be checked for updates every 30 seconds and
 * re-read if it has changed.
 */
public final class IPBanList implements Runnable
{
	private HashSet<String> m_ips = new HashSet<>();
	private File m_file;
	private long m_lastRead = 0;
	private LoggerInterface m_logger;
	
	public IPBanList(final String fileName, final LoggerInterface logger)
	{
		m_file = new File(fileName);
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
					final HashSet<String> ips = new HashSet<>();
					final List<String> lines = FileUtils.readLines(m_file, Charset.defaultCharset());
					for (final String line : lines)
					{
						if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#"))
							ips.add(StringUtils.split(line, '#')[0].trim());
					}
					m_ips = ips;
					m_logger.log(LogLevel.INFO, LogType.SYSTEM, "%s banned IP addresses read from %s", Integer.toString(m_ips.size()), m_file.getName());
			    	m_lastRead = System.currentTimeMillis();
				}
				Thread.sleep(30000);
			}
		}
		catch (final IOException | InterruptedException ex)
		{
			m_logger.log(LogLevel.ERROR, LogType.SYSTEM, ex, "Cannot read banned IP addresses from %s. Error is %s", m_file.getName(), ex.getMessage());
		}
	}
	
	/**
	 * Is the IP address banned.
	 * @param ipAddress IP address to check
	 * @return true/false.
	 */
	public boolean isBanned(final String ipAddress)
	{
		return m_ips.contains(ipAddress);
	}
}
