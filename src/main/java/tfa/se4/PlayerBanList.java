package tfa.se4;

import java.io.File;
import java.io.IOException;
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
 * 
 * Duplicates will automatically be ignored.
 * 
 * A banned player is automatically kicked.
 * 
 * The file will be checked for updates every 30 seconds and
 * re-read if it has changed.
 */
public final class PlayerBanList implements Runnable
{
	public static class Ban
	{
		public String steamID;
		public String name;
		public String reason;
	}

	private Map<String,Ban> m_bans = new HashMap<>();
	private File m_file;
	private long m_lastRead = 0;
	private LoggerInterface m_logger;
	
	public PlayerBanList(final String fileName, final LoggerInterface logger)
	{
		m_file = new File(fileName);
		m_logger = logger;
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
				if (FileUtils.isFileNewer(m_file, m_lastRead))
				{
					final Map<String,Ban> bans = new HashMap<>();
					final List<String> lines = FileUtils.readLines(m_file, Charset.defaultCharset());
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
						    	m_logger.log(LogLevel.INFO, LogType.SYSTEM, "Invalid ban entry '%s'  bans read from %s was ignored", line, m_file.getName());
							}
						}
						
					}
					m_bans = bans;
			    	m_logger.log(LogLevel.INFO, LogType.SYSTEM,"%s player bans read from %s", Integer.toString(m_bans.size()), m_file.getName());
			    	m_lastRead = System.currentTimeMillis();
				}
				//noinspection BusyWait
				Thread.sleep(30000);
			}
		}
		catch (final IOException | InterruptedException ex)
		{
	    	m_logger.log(LogLevel.ERROR, LogType.SYSTEM, ex,"Cannot read player bans from %s. Error is %s", m_file.getName(), ex.getMessage());
		}			
	}
	
	/**
	 * Get the ban information.
	 * @param steamID steam ID to check
	 * @return true/false.
	 */
	public Ban getBan(final String steamID)
	{
		return m_bans.get(steamID);
	}
}
