package tfa.se4.gui;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.util.Callback;

import tfa.se4.KickBanReasons;
import tfa.se4.json.Player;

public class ServerPaneController implements Initializable {
    @FXML private SplitPane paneId;
    @FXML private Label statusLabel;
    @FXML private Label mapLabel;
    @FXML private Label modeLabel;
    @FXML private Label scoreLabel;
    @FXML private Label timeLabel;
    @FXML private Label serverNameLabel;
    @FXML private Label fpsLabel;
    @FXML private Label bytesSentLabel;
    @FXML private Label bytesReceivedLabel;
    @FXML private TableView<Player> playersTable;
    @FXML private TableColumn<Player,String> name;
    @FXML private TableColumn<Player,String> steamID;
    @FXML private TableColumn<Player,String> ipV4;
    @FXML private TableColumn<Player,Long> latency;
    @FXML private TableColumn<Player,Long> score;
    @FXML private TableColumn<Player,Long> deaths;
    @FXML private TableColumn<Player,Long> kills;
    @FXML private TableColumn<Player,Long> assists;
    @FXML private TableColumn<Player,Double> longestShot;
    @FXML private ListView<String> logList;

    private MonitoredServerConnection m_connection;
    private KickBanReasons m_reasons;

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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        m_reasons = new KickBanReasons(m_connection);
        statusLabel.textProperty().bind(m_connection.getModel().stateProperty());
        mapLabel.textProperty().bind(m_connection.getModel().mapProperty());
        modeLabel.textProperty().bind(m_connection.getModel().modeProperty());
        scoreLabel.textProperty().bind(m_connection.getModel().scoreLimitProperty());
        timeLabel.textProperty().bind(m_connection.getModel().timeLimitProperty());
        serverNameLabel.textProperty().bind(m_connection.getModel().serverNameProperty());
        fpsLabel.textProperty().bind(m_connection.getModel().fpsProperty());
        bytesSentLabel.textProperty().bind(m_connection.getModel().bytesSentProperty());
        bytesReceivedLabel.textProperty().bind(m_connection.getModel().bytesReceivedProperty());

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
        longestShot.setCellFactory(new ColumnFormatter<Player, Double>(new DecimalFormat("0.00")));
        playersTable.setItems(m_connection.getModel().getPlayers());

        playersTable.setRowFactory(
            new Callback<TableView<Player>, TableRow<Player>>() {
                @Override
                public TableRow<Player> call(TableView<Player> tableView) {
                    final TableRow<Player> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    MenuItem profileItem = new MenuItem("Profile ...");
                    profileItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            SE4AdminGUI.showDocument("https://steamcommunity.com/profiles/" + row.getItem().getSteamId());
                        }
                    });
                    MenuItem kickItem = new MenuItem("Kick ...");
                    kickItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            final List<String> reasons = m_reasons.getReasons();
                            ChoiceDialog<String> dialog = new ChoiceDialog<>(reasons.get(0), reasons);
                            dialog.setTitle("Kick " + row.getItem().getName());
                            dialog.setHeaderText("Select reason for kicking " + row.getItem().getName());
                            dialog.setContentText("Reason:");

                            Optional<String> result = dialog.showAndWait();
                            result.ifPresent(r -> m_connection.kickPlayer(row.getItem(), r));
                        }
                    });
                    MenuItem banItem = new MenuItem("Ban ...");
                    banItem.setOnAction(new EventHandler<ActionEvent>() {
                        @Override
                        public void handle(ActionEvent event) {
                            final List<String> reasons = m_reasons.getReasons();
                            ChoiceDialog<String> dialog = new ChoiceDialog<>(reasons.get(0), reasons);
                            dialog.setTitle("Ban " + row.getItem().getName());
                            dialog.setHeaderText("Select reason for banning " + row.getItem().getName());
                            dialog.setContentText("Reason:");

                            Optional<String> result = dialog.showAndWait();
                            result.ifPresent(r -> m_connection.banPlayer(row.getItem(), r));
                        }
                    });
                    rowMenu.getItems().addAll(profileItem, kickItem, banItem);

                    // only display context menu for non-null items:
                    row.contextMenuProperty().bind(
                            Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                    .then(rowMenu)
                                    .otherwise((ContextMenu)null));
                    return row;
                }
            });

        // log pane
        logList.setItems(m_connection.getModel().getLogLines());
    }
}
