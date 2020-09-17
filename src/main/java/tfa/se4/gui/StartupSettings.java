package tfa.se4.gui;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FileUtils;

/**
 * Class to remember settings between sessions. These are saved when program exits and re-read on
 * startup.
 */
public class StartupSettings {

    public static class Settings
    {
        public double x;
        public double y;
        public double width;
        public double height;
        public List<String> filesToReOpen;
        private Settings()
        {

        }
    }

    // File to save settings to
    private static final File SETTINGS_FILE = new File(System.getProperty("user.dir"), ".saved_settings");

    // Version control for settings file in case we add more later on.
    private static final String VERSION = "V1";

    /**
     * Save stuff
     * @param openFiles List of files for open server properties
     * @param x X location of main window
     * @param y Y location of main window
     * @param width width of main window
     * @param height height of main window
     */
    public static void saveSettings(final List<String> openFiles, final double x, final double y, final double width, final double height)
    {
        final ArrayList<String> toWrite = new ArrayList<>();
        toWrite.add(VERSION);
        toWrite.add(Double.toString(x));
        toWrite.add(Double.toString(y));
        toWrite.add(Double.toString(width));
        toWrite.add(Double.toString(height));
        toWrite.addAll(openFiles);

        try {
            FileUtils.writeLines(SETTINGS_FILE, toWrite);
        } catch (IOException e) {
            // ignore. This is at program exit anyway.
        }
    }

    /**
     * Fetch previously saved settings.
     * @return Settings or null if not found or contain junk data.
     */
    public static Settings readSettings()
    {
        Settings result = new Settings();

        try
        {
            final List<String> lines = FileUtils.readLines(SETTINGS_FILE, Charset.defaultCharset());
            if (lines.size() < 5)
                return null;

            // first line always the version flag. If we change the file format later on,
            // need to retain ability to read old versions.
            result.x = Double.parseDouble(lines.get(1));
            result.y = Double.parseDouble(lines.get(2));
            result.width = Double.parseDouble(lines.get(3));
            result.height = Double.parseDouble(lines.get(4));
            if (lines.size()  == 5)
                result.filesToReOpen = new ArrayList<String>();
            else
                result.filesToReOpen = lines.subList(5, lines.size());

            return result;
        }
        catch (final NumberFormatException | IOException e)
        {
            // ignore
        }
        return null;
    }
}
