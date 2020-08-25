package tfa.se4;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * Read list of banned IP addresses from file.  Format is one
 * IP address per line. Duplicates will automatically be ignored.
 * 
 * The file will be checked for updates every 30 seconds and
 * re-read if it has changed.
 */
public final class IPBanList implements Runnable
{
	private HashSet<String> m_ips = new HashSet<String>();
	private File m_file;
	private long m_lastRead = 0;
	private Logger m_logger;
	
	public IPBanList(final String fileName, final Logger logger)
	{
		m_file = new File(fileName);
		m_logger = logger;
		new Thread(this).start();
	}
	
	@Override
	public void run()
	{
		try
		{
			while (true)
			{	
				if (FileUtils.isFileNewer(m_file, m_lastRead))
				{
					final HashSet<String> ips = new HashSet<String>();
					final List<String> lines = FileUtils.readLines(m_file, Charset.defaultCharset());
					for (final String line : lines)
					{
						if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#"))
							ips.add(StringUtils.split(line, '#')[0].trim());
					}
					m_ips = ips;
			    	m_logger.info("SYSTEM||{} banned IP addresses read from {}", m_ips.size(), m_file.getName());
			    	m_lastRead = System.currentTimeMillis();
				}
				Thread.sleep(30000);
			}
		}
		catch (final IOException | InterruptedException ex)
		{
	    	m_logger.error("SYSTEM||Cannot read banned IP addresses from {}. Error is {}", m_file.getName(), ex.getMessage());
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
