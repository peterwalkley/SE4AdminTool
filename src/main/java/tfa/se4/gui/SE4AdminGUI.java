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

    @Override
    public void start(Stage primaryStage) throws Exception {
        s_hostServices = HostServicesFactory.getInstance(this);
        s_primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainPane.fxml"));
        primaryStage.setTitle("SE4 Admin");
        primaryStage.setScene(new Scene(root, 800, 600));
        primaryStage.show();
    }

    /**
     * Show URL in system browser.
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

    public static void main(String[] args) {
        launch(args);
    }
}
