package tfa.se4.gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SE4AdminGUI extends Application
{
    private static Stage s_primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        s_primaryStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getClassLoader().getResource("MainPane.fxml"));
        primaryStage.setTitle("SE4 Admin");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.setOnCloseRequest(e -> closedown());
        primaryStage.show();
    }

    public static Stage getPrimaryStage()
    {
        return s_primaryStage;
    }

    /**
     * Do tidy shutdown of anything we started.
     */
    private void closedown()
    {
        //TODO: close all opened tabs and shutdown threads
    }
    public static void main(String[] args) {
        launch(args);
    }
}
