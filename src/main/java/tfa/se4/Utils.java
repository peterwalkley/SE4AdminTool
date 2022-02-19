package tfa.se4;

import tfa.se4.logger.LoggerInterface;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

/**
 * General utils.
 */
public final class Utils {

    private Utils() {
        // Nothing to do
    }

    /**
     * Thread.sleep call that swallows the pointless InterruptedException.
     * @param millis Milliseconds to sleep
     */
    public static void sleep(final long millis)
    {
        try
        {
            Thread.sleep(millis);
        }
        catch (final InterruptedException ex) //NOSONAR We don't care about interruption here. It never happens.
        {
            // Do nothing
        }
    }

    /**
     * Format a logger message.
     * @param level Log level e.g. DEBUG
     * @param type Type e.g. IPBAN
     * @param t  Optional throwable for stack trace
     * @param message Message in MessageFormat string format
     * @param args Optional args for the message format
     * @return Formatted message as a string
     */
    public static String formatLogMessage(LoggerInterface.LogLevel level, LoggerInterface.LogType type, Throwable t, String message, Object[] args) {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(Instant.now().toString());
        sb.append('|');
        sb.append(level.label);
        sb.append('|');
        sb.append(type.label);
        sb.append('|');
        sb.append(args == null ? message : String.format(message, args));

        if (t != null)
        {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            sb.append('\n');
            sb.append(sw);
        }
        return sb.toString();
    }

}
