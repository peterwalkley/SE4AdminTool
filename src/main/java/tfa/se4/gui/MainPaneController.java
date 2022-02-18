package tfa.se4.gui;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.stage.FileChooser;
import tfa.se4.Options;
import tfa.se4.Utils;
import tfa.se4.VersionUtils;

public class MainPaneController implements Initializable
{
    @FXML
    private TabPane tabPane;
    @FXML
    private Button connectButton;
    @FXML
    private Button closeButton;
    @FXML
    private Hyperlink statusHyperlink;
    @FXML
    private Label statusLabel;
    @FXML
    private CheckBox darkTheme;

    @Override
    public void initialize(URL location, ResourceBundle resources)
    {
        SE4AdminGUI.getPrimaryStage().setOnCloseRequest(e -> closeButtonClicked());
        final StartupSettings.Settings settings = StartupSettings.readSettings();
        if (settings != null && settings.filesToReOpen != null)
        {
            settings.filesToReOpen.forEach(f -> openTab(new File(f)));
            darkTheme.setSelected(settings.isDarkTheme);
        }

        statusHyperlink.setOnAction(x -> {
            SE4AdminGUI.showDocument(statusHyperlink.getText());
            statusHyperlink.setVisited(false);
        });

        Platform.runLater(this::darkThemeClicked);
        Platform.runLater(this::checkForUpdate);
    }

    /**
     * Check online at GitHub to see if there is an updated version.
     */
    private void checkForUpdate()
    {
        statusLabel.setText("Checking for new version");
        switch (VersionUtils.checkForNewVersion())
        {
            case ON_LATEST:
                statusLabel.setText("Version is up to date. For help and to report issues, please check the website at:");
                break;
            case NEW_VERSION:
                statusLabel.setText("A new version is available. Please check the website at:");
                statusLabel.setStyle("-fx-font-weight: bold;");
                break;
            case CHECK_ERROR:
                statusLabel.setText("Unable to check for new version");
                break;
        }
    }

    /**
     * Handler for the connect button being clicked.
     */
    public void connectButtonClicked()
    {
        FileChooser chooser = new FileChooser();
        chooser.setInitialDirectory(new File(System.getProperty("user.dir")));
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Setting Files", "*.properties")
        );

        File selected = chooser.showOpenDialog(SE4AdminGUI.getPrimaryStage());
        if (selected == null)
        {
            return;
        }

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
        {
            tabPane.getSelectionModel().select(newTab);
        }
    }

    /**
     * Helper to open a tab for a given configuration file.
     * @param selected File to open
     * @return New tab or null when a problem opening
     */
    private Tab openTab(File selected)
    {
        final Tab newTab = new Tab(selected.getName());
        newTab.setId(selected.getAbsolutePath());

        boolean warnSettings = false;
        try
        {
            final Options opts = new Options(selected.getAbsolutePath());
            if (opts.hasInvalidSteamSettings())
            {
                opts.makeSteamSettingsConsistent();
                warnSettings = true;
            }
            final MonitoredServerConnection conn = new MonitoredServerConnection(opts);
            final ServerPaneController controller = new ServerPaneController(conn);
            newTab.setOnCloseRequest(e -> controller.close());
            newTab.setUserData(conn);
            FXMLLoader loader = new FXMLLoader(getClass().getClassLoader().getResource("ServerPane.fxml"));
            loader.setController(controller);
            newTab.setContent(loader.load());
        }
        catch (Exception e)
        {
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setHeaderText("Unable to initialise connection");
            errorAlert.setContentText(e.getLocalizedMessage());
            errorAlert.showAndWait();
            return null;
        }

        if (warnSettings)
        {
            Alert steamAlert = new Alert(Alert.AlertType.WARNING);
            steamAlert.setHeaderText("Configuration is Inconsistent");
            steamAlert.setContentText("Kicking of VAC or Game banned player and managing closed profiles requires a steam API key for checks to be performed.\nThese features will be disabled.  Please update your configuration.");
            steamAlert.showAndWait();
        }

        tabPane.getTabs().add(newTab);
        return newTab;
    }

    /**
     * Handler for close button. Close all the tabs and exit.
     */
    public void closeButtonClicked()
    {
        final List<String> openTabFileNames = new ArrayList<>();
        tabPane.getTabs().forEach(t -> openTabFileNames.add(t.getId()));
        StartupSettings.saveSettings(openTabFileNames,
                SE4AdminGUI.getPrimaryStage().getX(),
                SE4AdminGUI.getPrimaryStage().getY(),
                SE4AdminGUI.getPrimaryStage().getWidth(),
                SE4AdminGUI.getPrimaryStage().getHeight(),
                darkTheme.isSelected());
        tabPane.getTabs().forEach(t -> ((MonitoredServerConnection) t.getUserData()).closeConnection());

        // Give threads above a little time to close.
        Utils.sleep(2000);

        SE4AdminGUI.getPrimaryStage().close();
        System.exit(0);
    }

    /**
     * Handler for the dark theme being toggled.
     */
    public void darkThemeClicked()
    {
        SE4AdminGUI.setDarkTheme(darkTheme.isSelected());
    }
}
