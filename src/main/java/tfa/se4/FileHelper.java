package tfa.se4;

import java.io.File;

import org.apache.commons.io.FileUtils;

public final class FileHelper {

    private FileHelper() {}

    /**
     * Get File object for file name assumed to be in the 'config' directory.
     * @param name Name of file to look for.
     * @return File object
     */
    public static File getConfigFile(final String name)
    {
        final File curDir = new File(System.getProperty("user.dir"));
        return new File(curDir.getPath() + File.separator + "config" + File.separator + name);
    }

    /**
     * Check to see if a file in the 'config' directory exists with the given name.
     * If it doesn't, try and create one from the example present in that directory.
     *
     * @param name Name of file - and we will try and file example_<xxx> to clone.
     */
    public static void cloneConfigFileFromExampleIfMissing(final String name)
    {
        final File f = getConfigFile(name);
        if (f.isFile())
            return;

        final File example = getConfigFile("example_" + name);
        if (example.isFile())
        {
            try
            {
                FileUtils.copyFile(example, f);
            }
            catch (final Exception ex)
            {
                // ignore - we can't do anything.
            }
        }
    }
}
