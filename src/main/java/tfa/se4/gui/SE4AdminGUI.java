package tfa.se4.gui;

import com.sun.deploy.uitoolkit.impl.fx.HostServicesFactory;
import com.sun.javafx.application.HostServicesDelegate;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SE4AdminGUI extends Application
{
    private static Stage s_primaryStage;
    private static HostServicesDelegate s_hostServices;
    private static boolean s_isDarkTheme;

    @Override
    public void start(Stage primaryStage) throws Exception
    {
        s_hostServices = HostServicesFactory.getInstance(this);
        s_primaryStage = primaryStage;
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
        s_hostServices.showDocument(url);
    }

    public static Stage getPrimaryStage()
    {
        return s_primaryStage;
    }

    public static void setDarkTheme(final boolean isSet)
    {
        s_isDarkTheme = isSet;
        if (s_isDarkTheme)
            getPrimaryStage().getScene().getStylesheets().add("dark-theme.css");
        else
            getPrimaryStage().getScene().getStylesheets().remove("dark-theme.css");
    }

    public static boolean isDarkTheme() { return s_isDarkTheme; }

    public static void main(String[] args)
    {
        launch(args);
    }
}
