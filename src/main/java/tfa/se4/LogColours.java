package tfa.se4;

import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import tfa.se4.logger.LoggerInterface;

/**
 * Class to handle colour choices for the log window.
 */
public final class LogColours
{
    private static List<LogColour> sLogColours = null;
    private static Color sDefault = Color.BLACK;
    private static Color sDefaultChat = Color.BLACK;

    public static class LogColour {
        public Pattern pattern; //NOSONAR This is a simple bean holder
        public Color colour; //NOSONAR This is a simple bean holder

        private LogColour(final Pattern p, final Color c)
        {
            pattern = p;
            colour = c;
        }
    }

    private LogColours()
    {
        // Prevent instantiation
    }

    /**
     * Default colour for text.
     * @param logger Logger if problem
     * @return Default.
     */
    public static Color getDefault(final LoggerInterface logger)
    {
        if (sLogColours == null)
        {
            readConfiguration(logger);
        }
        return sDefault;
    }

    /**
     * Default colour for player text.
     * @param logger Logger if problem
     * @return Default.
     */
    public static Color getDefaultChat(final LoggerInterface logger)
    {
        if (sLogColours == null)
        {
            readConfiguration(logger);
        }
        return sDefaultChat;
    }

    /**
     * Get configured mappings of text string to colour.
     * @param logger Logger if problem
     * @return List of colours
     */
    public static  List<LogColour> getColours(final LoggerInterface logger)
    {
        if (sLogColours == null)
        {
            readConfiguration(logger);
        }

        return sLogColours;
    }

    /**
     * Read the log colours configuration.
     *
     * @param logger Logger
     */
    private static synchronized void readConfiguration(LoggerInterface logger)
    {
        final String filename = "log_colours.txt";
        FileHelper.cloneConfigFileFromExampleIfMissing(filename);
        final File toRead = FileHelper.getConfigFile(filename);

        final List<String> lines;
        try
        {
            lines = FileUtils.readLines(toRead, Charset.defaultCharset());
        }
        catch (final IOException e)
        {
            logger.log(LoggerInterface.LogLevel.INFO, LoggerInterface.LogType.SYSTEM, "Unable to read %s ", toRead.getName(), e.getLocalizedMessage());
            sLogColours = new ArrayList<>();
            return;
        }

        final List<LogColour> result = new ArrayList<>();
        for (final String line : lines)
        {
            if (StringUtils.isNotBlank(line) && !line.trim().startsWith("#"))
            {
                if (line.trim().startsWith("default") && line.contains("="))
                {
                    final Color c = extractColour(logger, line);
                    if (c != null)
                    {
                        sDefault = c;
                        continue;
                    }
                }

                if (line.trim().startsWith("chat") && line.contains("="))
                {
                    final Color c = extractColour(logger, line);
                    if (c != null)
                    {
                        sDefaultChat = c;
                        continue;
                    }
                }

                final String[] terms = StringUtils.split(line, '#');
                if (terms.length == 2)
                {
                    final Pattern p;
                    try
                    {
                        p = Pattern.compile(terms[0].trim());
                    }
                    catch (final PatternSyntaxException e)
                    {
                        logger.log(LoggerInterface.LogLevel.INFO, LoggerInterface.LogType.SYSTEM, "Invalid pattern %s, %s", terms[0].trim(), e.getLocalizedMessage());
                        continue;
                    }

                    final Color c = parseColour(logger, terms[1]);
                    if (c != null)
                    {
                        result.add(new LogColour(p, c));
                    }
                }
                else
                {
                    logger.log(LoggerInterface.LogLevel.INFO, LoggerInterface.LogType.SYSTEM, "Invalid log colour entry '%s' was ignored", line);
                }
            }
        }

        sLogColours = result;
    }

    /**
     * Get the colour part our from a line in form  XXX = <some colour>
     * @param logger Logger
     * @param line Line to parse
     * @return Color or null if we can't get one
     */
    private static Color extractColour(final LoggerInterface logger, final String line)
    {
        final String[] terms = StringUtils.split(line, '=');
        if (terms.length == 2)
        {
            return parseColour(logger, terms[1]);
        }
        return null;
    }

    /**
     * Parse a colour from string. Log if it fails and return null.
     * @param logger Logger to log to
     * @param s String to try and parse as a colour
     * @return Color or null if it's not parseable.
     */
    private static Color parseColour(final LoggerInterface logger, final String s)
    {
        try
        {
            return Color.web(s.trim());
        }
        catch (final Exception e)
        {
            logger.log(LoggerInterface.LogLevel.INFO, LoggerInterface.LogType.SYSTEM, "Invalid colour specification %s, %s", s.trim(), e.getLocalizedMessage());
            return null;
        }
    }
}
