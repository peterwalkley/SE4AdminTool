package tfa.se4.gui;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import tfa.se4.Options;

public class MainPaneController implements Initializable {

    @FXML private TabPane tabPane;
    @FXML private Button connectButton;
    @FXML private Button closeButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        SE4AdminGUI.getPrimaryStage().setOnCloseRequest(e -> closeButtonClicked());
        final StartupSettings.Settings settings = StartupSettings.readSettings();
        if (settings != null && settings.filesToReOpen != null)
        {
            settings.filesToReOpen.forEach(f -> openTab(new File(f)));
        }
    }

    public void connectButtonClicked() {

        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Setting Files", "*.properties")
        );
        File selected = chooser.showOpenDialog(SE4AdminGUI.getPrimaryStage());
        if (selected == null)
            return;

        // If we've already got this server being monitored. Select the tab and exit.
        for (final Tab t : tabPane.getTabs())
        {
            if (selected.getAbsolutePath().equals(t.getId()))
            {
                tabPane.getSelectionModel().select(t);
                return;
            }
        }

        final Tab newTab = openTab(selected);
        if (newTab != null)
            tabPane.getSelectionModel().select(newTab);
    }

    private Tab openTab(File selected) {
        final Tab newTab = new Tab(selected.getName());
        newTab.setId(selected.getAbsolutePath());
        try
        {
            final Options opts = new Options(selected.getAbsolutePath());
            final MonitoredServerConnection conn = new MonitoredServerConnection(opts);
            final ServerPaneController controller = new ServerPaneController(conn);
            newTab.setOnCloseRequest(e -> controller.close());
            newTab.setUserData(conn);
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServerPane.fxml"));
            loader.setController(controller);
            newTab.setContent(loader.load());

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Unable to initialise connection");
            errorAlert.setContentText(e.getLocalizedMessage());
            errorAlert.showAndWait();
            return null;
        }

        tabPane.getTabs().add(newTab);
        return newTab;
    }

    /**
     * Handler for close button. Close all the tabs and exit.
     */
    public void closeButtonClicked() {
        final List<String> openTabFileNames = new ArrayList<>();
        tabPane.getTabs().forEach(t -> openTabFileNames.add(t.getId()));
        StartupSettings.saveSettings(openTabFileNames,
                SE4AdminGUI.getPrimaryStage().getX(),
                SE4AdminGUI.getPrimaryStage().getY(),
                SE4AdminGUI.getPrimaryStage().getWidth(),
                SE4AdminGUI.getPrimaryStage().getHeight());
        tabPane.getTabs().forEach(t -> ((MonitoredServerConnection)t.getUserData()).closeConnection());

        // Give threads above a little time to close.
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            //ignore
        }
        SE4AdminGUI.getPrimaryStage().close();
        System.exit(0);
    }
}
