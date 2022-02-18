package tfa.se4.gui;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SE4AdminGUI extends Application
{
    private static Stage sPrimaryStage;
    private static HostServices sHostServices;
    private static boolean sIsDarkTheme;

    private static void doInit(HostServices hostServices, Stage primaryStage)
    {
        sHostServices = hostServices;
        sPrimaryStage = primaryStage;
    }

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        doInit(getHostServices(), primaryStage);
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainPane.fxml"));
        primaryStage.setTitle("SE4 Server Administrator " + tfa.se4.Version.BUILD_VERSION);
        final StartupSettings.Settings settings = StartupSettings.readSettings();

        if (settings == null)
        {
            primaryStage.setScene(new Scene(root, 1000, 600));
        }
        else
        {
            primaryStage.setScene(new Scene(root, settings.width, settings.height));
            primaryStage.setX(settings.x);
            primaryStage.setY(settings.y);

        }
        primaryStage.show();
    }

    /**
     * Show URL in system browser.
     *
     * @param url URL to show
     */
    public static void showDocument(final String url)
    {
        sHostServices.showDocument(url);
    }

    public static Stage getPrimaryStage()
    {
        return sPrimaryStage;
    }

    public static void setDarkTheme(final boolean isSet)
    {
        sIsDarkTheme = isSet;
        if (sIsDarkTheme)
            getPrimaryStage().getScene().getStylesheets().add("dark-theme.css");
        else
            getPrimaryStage().getScene().getStylesheets().remove("dark-theme.css");
    }

    public static boolean isDarkTheme() { return sIsDarkTheme; }

    public static void main(String[] args)
    {
        launch(args);
    }
}
