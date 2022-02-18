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
public final class StartupSettings
{

    public static class Settings
    {
        public double x; //NOSONAR This is a simple bean holder
        public double y; //NOSONAR This is a simple bean holder
        public double width; //NOSONAR This is a simple bean holder
        public double height; //NOSONAR This is a simple bean holder
        public List<String> filesToReOpen; //NOSONAR This is a simple bean holder
        public boolean isDarkTheme; //NOSONAR This is a simple bean holder

        private Settings()
        {

        }
    }

    // File to save settings to
    private static final File SETTINGS_FILE = new File(System.getProperty("user.dir"), ".saved_settings");

    // Original settings file version.
    private static final String VERSION_1 = "V1";

    //V2 file version. Added dark theme
    private static final String VERSION_2 = "V2";

    private StartupSettings()
    {
        // Prevent instantiation
    }

    /**
     * Save stuff
     *
     * @param openFiles List of files for open server properties
     * @param x         X location of main window
     * @param y         Y location of main window
     * @param width     width of main window
     * @param height    height of main window
     * @param darkTheme Whether dark theme enabled or not
     */
    public static void saveSettings(final List<String> openFiles, final double x, final double y, final double width, final double height, final boolean darkTheme)
    {
        final ArrayList<String> toWrite = new ArrayList<>();
        toWrite.add(VERSION_2);
        toWrite.add(Double.toString(x));
        toWrite.add(Double.toString(y));
        toWrite.add(Double.toString(width));
        toWrite.add(Double.toString(height));
        toWrite.add(Boolean.toString(darkTheme));
        toWrite.addAll(openFiles);

        try
        {
            FileUtils.writeLines(SETTINGS_FILE, toWrite);
        }
        catch (IOException e)
        {
            // ignore. This is at program exit anyway.
        }
    }

    /**
     * Fetch previously saved settings.
     *
     * @return Settings or null if not found or contain junk data.
     */
    public static Settings readSettings()
    {
        try
        {
            final List<String> lines = FileUtils.readLines(SETTINGS_FILE, Charset.defaultCharset());
            if (lines.size() < 5)
            {
                return null;
            }

            // first line is always the version flag. If we change the file format later on,
            if (VERSION_1.equals(lines.get(0)))
                return getFromV1Version(lines);
            else if (VERSION_2.equals(lines.get(0)))
                return getFromV2Version(lines);
        }
        catch (final NumberFormatException | IOException e)
        {
            // ignore
        }
        return null;
    }

    /**
     * Read version 1 settings file.
     * @param lines Lines read from saved data
     * @return Settings
     */
    private static Settings getFromV1Version(final List<String> lines)
    {
        Settings result = new Settings();
        result.x = Double.parseDouble(lines.get(1));
        result.y = Double.parseDouble(lines.get(2));
        result.width = Double.parseDouble(lines.get(3));
        result.height = Double.parseDouble(lines.get(4));
        result.isDarkTheme = false;
        if (lines.size() == 5)
        {
            result.filesToReOpen = new ArrayList<>();
        }
        else
        {
            result.filesToReOpen = lines.subList(5, lines.size());
        }
        return result;
    }

    /**
     * Read version 2 settings file.
     * @param lines Lines read from saved data.
     * @return Settings
     */
    private static Settings getFromV2Version(final List<String> lines)
    {
        Settings result = new Settings();
        result.x = Double.parseDouble(lines.get(1));
        result.y = Double.parseDouble(lines.get(2));
        result.width = Double.parseDouble(lines.get(3));
        result.height = Double.parseDouble(lines.get(4));
        result.isDarkTheme = Boolean.parseBoolean(lines.get(5));
        if (lines.size() == 6)
        {
            result.filesToReOpen = new ArrayList<>();
        }
        else
        {
            result.filesToReOpen = lines.subList(6, lines.size());
        }
        return result;
    }
}
