package tfa.se4;

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
}
