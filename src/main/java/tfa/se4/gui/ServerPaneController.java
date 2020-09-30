package tfa.se4.gui;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.paint.Color;
import javafx.util.Callback;

import org.apache.commons.lang3.StringUtils;
import tfa.se4.KickBanReasons;
import tfa.se4.json.Player;

public class ServerPaneController implements Initializable
{
    private static final List<String> S_COMMANDS = new ArrayList<>(Arrays.asList(
            "?", "Clear", "Exec", "Game.End", "Game.Info", "Game.Start", "Help", "IP", "ListCmds", "ListVars",
            "Lobby.AutoTriggerMapVote", "Lobby.ClearPassword", "Lobby.Password", "Lobby.RequestMapVote",
            "Lobby.StartTimer", "MapRotation.AddMap", "MapRotation.ClearMapRotation", "MapRotation.CurrentMap",
            "MapRotation.ListMapRotation", "MapRotation.ListMaps", "MapRotation.NextMap", "MapRotation.RemoveMap",
            "MapRotation.RotateMapIndex", "Quit", "RCon.IP", "RCon.Listen", "RCon.Port", "RCon.WebStatusUpdate",
            "Server.AddTimedText", "Server.AuthPort", "Server.ClearTimedText", "Server.GamePort", "Server.Host",
            "Server.Info", "Server.Kick", "Server.KickBan", "Server.KickBanIndex", "Server.KickBanSteamID",
            "Server.KickIndex", "Server.ListBans", "Server.ListPlayers", "Server.ListTimedText", "Server.LobbyPort",
            "Server.MoTD", "Server.Name", "Server.NetInfo", "Server.Say", "Server.TimedTextInterval", "Server.UnBan",
            "Server.UpdatePort", "Settings.AimAssist", "Settings.AmmoCount", "Settings.AutoBalance",
            "Settings.BinoTagging", "Settings.BulletCam", "Settings.BulletDrop", "Settings.BulletTrails",
            "Settings.DamageIndicators", "Settings.DefaultScoreLimit", "Settings.DefaultTimeLimit",
            "Settings.EmptyLungZoom", "Settings.ForceRespawn", "Settings.FriendlyFire", "Settings.Grenades",
            "Settings.HeadshotsOnly", "Settings.HealthItems", "Settings.Items", "Settings.LandMines",
            "Settings.MaxLatency", "Settings.MaxPlayers", "Settings.MeleeKills", "Settings.OneShotKills",
            "Settings.Panzerfausts", "Settings.Pistols", "Settings.PlayerHealth", "Settings.RadarThreats",
            "Settings.RespawnTime", "Settings.S-Mines", "Settings.SecondaryWeapons", "Settings.ShotTagging",
            "Settings.ShowPlayersKiller", "Settings.SideSwapping", "Settings.SingleBulletReload",
            "Settings.SniperRifles", "Settings.SniperScopeGlint", "Settings.SpawnProtectionTime",
            "Settings.SpectatorOverview", "Settings.Sprint", "Settings.SuddenDeath", "Settings.TNT",
            "Settings.TripMines", "Settings.WarmupTime", "Settings.WindStrength"
    ));

    @FXML
    private SplitPane paneId;
    @FXML
    private Label statusLabel;
    @FXML
    private Label mapLabel;
    @FXML
    private Label modeLabel;
    @FXML
    private Label scoreLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private Label serverNameLabel;
    @FXML
    private Label fpsLabel;
    @FXML
    private Label bytesSentLabel;
    @FXML
    private Label bytesReceivedLabel;
    @FXML
    private TableView<Player> playersTable;
    @FXML
    private TableColumn<Player, String> name;
    @FXML
    private TableColumn<Player, String> steamID;
    @FXML
    private TableColumn<Player, String> ipV4;
    @FXML
    private TableColumn<Player, Long> latency;
    @FXML
    private TableColumn<Player, Long> score;
    @FXML
    private TableColumn<Player, Long> deaths;
    @FXML
    private TableColumn<Player, Long> kills;
    @FXML
    private TableColumn<Player, Long> assists;
    @FXML
    private TableColumn<Player, Double> longestShot;
    @FXML
    private TableColumn<Player, String> location;
    @FXML
    private ListView<String> logList;
    @FXML
    private TextField commandText;
    @FXML
    private Button sendButton;

    private MonitoredServerConnection m_connection;
    private KickBanReasons m_reasons;

    public ServerPaneController(final MonitoredServerConnection connection)
    {
        m_connection = connection;
    }

    // close tab
    public void close()
    {
        m_connection.closeConnection();
    }

    @Override
    public void initialize(URL loc, ResourceBundle resources)
    {
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
        location.setCellValueFactory(new PropertyValueFactory<>("location"));
        if (!m_connection.ipStackSupported())
        {
            location.setVisible(false);
        }

        playersTable.setItems(m_connection.getModel().getPlayers());

        playersTable.setRowFactory(
                new Callback<TableView<Player>, TableRow<Player>>()
                {
                    @Override
                    public TableRow<Player> call(TableView<Player> tableView)
                    {
                        final TableRow<Player> row = new TableRow<>();
                        final ContextMenu rowMenu = new ContextMenu();
                        MenuItem profileItem = new MenuItem("Profile ...");
                        profileItem.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent event)
                            {
                                SE4AdminGUI.showDocument("https://steamcommunity.com/profiles/" + row.getItem().getSteamId());
                            }
                        });
                        MenuItem kickItem = new MenuItem("Kick ...");
                        kickItem.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent event)
                            {
                                final Player toKick = row.getItem();
                                final List<String> reasons = m_reasons.getReasons();
                                ChoiceDialog<String> dialog = new ChoiceDialog<>(reasons.get(0), reasons);
                                dialog.setTitle("Kick " + toKick.getName());
                                dialog.setHeaderText("Select reason for kicking " + toKick.getName());
                                dialog.setContentText("Reason:");

                                Optional<String> result = dialog.showAndWait();
                                result.ifPresent(r -> m_connection.kickPlayer(toKick, r));
                            }
                        });
                        MenuItem banItem = new MenuItem("Ban ...");
                        banItem.setOnAction(new EventHandler<ActionEvent>()
                        {
                            @Override
                            public void handle(ActionEvent event)
                            {
                                final Player toBan = row.getItem();
                                final List<String> reasons = m_reasons.getReasons();
                                ChoiceDialog<String> dialog = new ChoiceDialog<>(reasons.get(0), reasons);
                                dialog.setTitle("Ban " + toBan.getName());
                                dialog.setHeaderText("Select reason for banning " + toBan.getName());
                                dialog.setContentText("Reason:");

                                Optional<String> result = dialog.showAndWait();
                                result.ifPresent(r -> m_connection.banPlayer(toBan, r));
                            }
                        });
                        rowMenu.getItems().addAll(profileItem, kickItem, banItem);

                        // only display context menu for non-null items:
                        row.contextMenuProperty().bind(
                                Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                        .then(rowMenu)
                                        .otherwise((ContextMenu) null));
                        return row;
                    }
                });

        // log pane
        final ContextMenu logMenu = new ContextMenu();
        MenuItem copyAllItem = new MenuItem("Copy ALL to clipboard ...");
        copyAllItem.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
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
        });
        MenuItem copySelectedItem = new MenuItem("Copy selected to clipboard ...");
        copySelectedItem.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                final Clipboard clipboard = Clipboard.getSystemClipboard();
                final ClipboardContent content = new ClipboardContent();
                final StringBuilder sb = new StringBuilder(4096);
                logList.getSelectionModel().getSelectedItems().forEach(line ->
                {
                    sb.append(line);
                    sb.append('\n');
                });
                content.putString(sb.toString());
                clipboard.setContent(content);
            }
        });
        copySelectedItem.disableProperty().bind(logList.getSelectionModel().selectedItemProperty().isNull());
        MenuItem clearItem = new MenuItem("Clear log");
        clearItem.setOnAction(new EventHandler<ActionEvent>()
        {
            @Override
            public void handle(ActionEvent event)
            {
                m_connection.getModel().clearLog();
            }
        });
        logMenu.getItems().addAll(copyAllItem, copySelectedItem, clearItem);
        logList.setItems(m_connection.getModel().getLogLines());
        logList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logList.setContextMenu(logMenu);
        logList.setCellFactory(new Callback<ListView<String>, ListCell<String>>()
        {
            @Override
            public ListCell<String> call(ListView<String> param)
            {
                return new ColouredCell(m_connection);
            }
        });

        // command window section
        final ContextMenu commandsMenu = new ContextMenu();
        final Map<String, Menu> childMenus = new HashMap<>();
        S_COMMANDS.forEach(command -> {
            String parentName = null;
            String itemName = null;

            if (command.indexOf('.') != -1)
            {
                final String[] commandSeq = command.split("\\.");
                parentName = commandSeq[0];
                itemName = commandSeq[1];
            }
            else
            {
                itemName = command;
            }

            MenuItem item = new MenuItem(itemName);
            item.setOnAction(new EventHandler<ActionEvent>()
            {
                @Override
                public void handle(ActionEvent event)
                {
                    insertCommand(command);
                }
            });

            if (parentName == null)
            {
                commandsMenu.getItems().add(item);
            }
            else
            {
                if (childMenus.get(parentName) == null)
                {
                    final Menu m = new Menu(parentName);
                    childMenus.put(parentName, m);
                    commandsMenu.getItems().add(m);
                }
                childMenus.get(parentName).getItems().add(item);
            }
        });
        commandText.setContextMenu(commandsMenu);
        sendButton.disableProperty().bind(commandText.textProperty().isEmpty());
    }

    private void insertCommand(final String command)
    {
        String cmdText = commandText.getText();
        for (final String s : S_COMMANDS)
        {
            if (cmdText.contains(s))
            {
                String newCmd = cmdText.replace(s, command);
                commandText.setText(newCmd);
                commandText.positionCaret(command.length());
                return;
            }
        }

        commandText.setText(command + cmdText);
        commandText.positionCaret(command.length());
    }

    public void sendMessage()
    {
        if (StringUtils.isNotBlank(commandText.getText()))
        {
            logList.scrollTo(logList.getItems().size() - 1);
            m_connection.sendCommand(commandText.getText());
            commandText.clear();
        }
    }
}
