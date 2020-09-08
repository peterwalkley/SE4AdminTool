package tfa.se4.gui;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import tfa.se4.json.Player;

import java.net.URL;
import java.util.ResourceBundle;

public class ServerPaneController implements Initializable {
    @FXML private SplitPane paneId;
    @FXML private Label mapLabel;
    @FXML private Label modeLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private TableView<Player> playersTable;
    @FXML private TableColumn<Player,String> name;
    @FXML private TableColumn<Player,String> steamID;
    @FXML private TableColumn<Player,String> ipV4;
    @FXML private TableColumn<Player,Integer> latency;
    @FXML private TableColumn<Player,Integer> score;
    @FXML private TableColumn<Player,Integer> deaths;
    @FXML private TableColumn<Player,Integer> kills;
    @FXML private TableColumn<Player,Integer> assists;
    @FXML private TableColumn<Player,Double> longestShot;
    @FXML private Button profileButton;
    @FXML private Button kickButton;
    @FXML private Button banButton;
    @FXML private ListView<String> logList;

    private MonitoredServerConnection m_connection;

    public ServerPaneController(final MonitoredServerConnection connection)
    {
        m_connection = connection;
    }

    public void clearLog() {
        m_connection.getModel().clearLog();
    }
    public void clipboard() {
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        final ClipboardContent content = new ClipboardContent();
        final StringBuilder sb = new StringBuilder(4096);
        m_connection.getModel().getRawLogLines().forEach(line ->
                {
                sb.append(line);
                sb.append('\n');
        });
        content.putString(sb.toString());
        clipboard.setContent(content);
    }

    // close tab
    public void close() {
        m_connection.closeConnection();
    }
    public void showProfile() {
        //TODO:
        System.out.println("profile button clicked");
    }
    public void kick() {
        //TODO:
        System.out.println("kick button clicked");
    }
    public void ban() {
        //TODO:
        System.out.println("ban button clicked");
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mapLabel.textProperty().bind(m_connection.getModel().mapProperty());
        modeLabel.textProperty().bind(m_connection.getModel().modeProperty());
        scoreLabel.textProperty().bind(m_connection.getModel().scoreLimitProperty());
        timeLabel.textProperty().bind(m_connection.getModel().timeLimitProperty());

        // Players table
        name.setCellValueFactory(new PropertyValueFactory<>("name"));
        steamID.setCellValueFactory(new PropertyValueFactory<>("steamId"));
        ipV4.setCellValueFactory(new PropertyValueFactory<>("iPv4"));
        latency.setCellValueFactory(new PropertyValueFactory<>("latency"));
        score.setCellValueFactory(new PropertyValueFactory<>("score"));
        deaths.setCellValueFactory(new PropertyValueFactory<>("deaths"));
        kills.setCellValueFactory(new PropertyValueFactory<>("kills"));
        assists.setCellValueFactory(new PropertyValueFactory<>("assists"));
        longestShot.setCellValueFactory(new PropertyValueFactory<>("longestShot"));
        playersTable.setItems(m_connection.getModel().getPlayers());

        // log pane
        logList.setItems(m_connection.getModel().getLogLines());
    }
}
