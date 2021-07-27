package tfa.se4;

import java.io.IOException;

import org.apache.http.client.fluent.Request;

/**
 * Helper to check at GitHub to see if there is a newer version available.
 */
public final class VersionUtils
{
    public enum Result
    {
        CHECK_ERROR(),
        NEW_VERSION(),
        ON_LATEST();

        Result()
        {
        }
    }

    /**
     * Check for a new version of the code.
     * @return status
     */
    public static Result checkForNewVersion()
    {
        final String url = "https://raw.githubusercontent.com/peterwalkley/SE4AdminTool/master/latest/version.txt";

        try
        {
            final int[] latest = parseVersion(Request.Get(url).connectTimeout(2000).socketTimeout(2000).execute().returnContent().asString().trim());
            final int[] build = parseVersion(Version.BUILD_VERSION);

            for (int i = 0; i < latest.length; i++)
            {
                if (build[i] > latest[i])
                {
                    System.out.println("Detected developer build");
                    return Result.CHECK_ERROR;

                }
                if (latest[i] > build[i])
                {
                    return Result.NEW_VERSION;
                }
            }
            return Result.ON_LATEST;
        }
        catch (IOException e)
        {
            return Result.CHECK_ERROR;
        }
    }

    private static int[] parseVersion(final String s)
    {
        final String[] terms = s.split("\\.");
        int[] result = new int[terms.length];
        for (int i = 0; i < terms.length; i++)
        {
            result[i] = Integer.parseInt(terms[i]);
        }
        return result;
    }
}
