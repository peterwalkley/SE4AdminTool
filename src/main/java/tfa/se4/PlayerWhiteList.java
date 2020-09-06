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
 * 
 * A white listed player will not be checked for VAC or Game bans.
 * The file will be checked for updates every 30 seconds and
 * re-read if it has changed.
 */
public final class PlayerWhiteList implements Runnable
{
	private HashSet<String> m_ids = new HashSet<String>();
	private File m_file;
	private long m_lastRead = 0;
	private LoggerInterface m_logger;
	
	public PlayerWhiteList(final String fileName, final LoggerInterface logger)
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
					final HashSet<String> ids = new HashSet<String>();
					final List<String> lines = FileUtils.readLines(m_file, Charset.defaultCharset());
					for (final String line : lines)
					{
						if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#"))
						{
							ids.add(StringUtils.split(line, '#')[0].trim());
						}
					}
					
					m_ids = ids;
			    	m_logger.log(LogLevel.INFO, LogType.SYSTEM,"whitelisted steam IDs read from %s", Integer.toString(m_ids.size()), m_file.getName());
			    	m_lastRead = System.currentTimeMillis();
				}
				Thread.sleep(30000);
			}
		}
		catch (final IOException | InterruptedException ex)
		{
			m_logger.log(LogLevel.ERROR, LogType.SYSTEM, ex,"Cannot read player whitelist from %s. Error is %s", m_file.getName(), ex.getMessage());
		}
	}
	
	/**
	 * Is the steam ID white listed
	 * @param steamID steam ID to check
	 * @return true/false.
	 */
	public boolean isWhitelisted(final String steamID)
	{
		return m_ids.contains(steamID);
	}
}
