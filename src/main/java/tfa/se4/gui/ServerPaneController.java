package tfa.se4.gui;

import java.net.URL;
import java.text.DecimalFormat;
import java.util.*;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

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
    private Label remainingLabel; // the label that says remaining time
    @FXML
    private Label timeLeftLabel; // the VALUE of the remaining time
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
    private TableColumn<Player, String> playedHours;
    @FXML
    private TableColumn<Player, String> location;
    @FXML
    private ListView<String> logList;
    @FXML
    private TextField commandText;
    @FXML
    private Button sendButton;

    private final MonitoredServerConnection mConnection;

    public ServerPaneController(final MonitoredServerConnection connection)
    {
        mConnection = connection;
    }

    // close tab
    public void close()
    {
        mConnection.closeConnection();
    }

    @Override
    public void initialize(URL loc, ResourceBundle resources)
    {
        final KickBanReasons kickBanReasons = new KickBanReasons(mConnection);
        statusLabel.textProperty().bind(mConnection.getModel().stateProperty());
        mapLabel.textProperty().bind(mConnection.getModel().mapProperty());
        modeLabel.textProperty().bind(mConnection.getModel().modeProperty());
        scoreLabel.textProperty().bind(mConnection.getModel().scoreLimitProperty());
        timeLabel.textProperty().bind(mConnection.getModel().timeLimitProperty());
        serverNameLabel.textProperty().bind(mConnection.getModel().serverNameProperty());
        fpsLabel.textProperty().bind(mConnection.getModel().fpsProperty());
        bytesSentLabel.textProperty().bind(mConnection.getModel().bytesSentProperty());
        bytesReceivedLabel.textProperty().bind(mConnection.getModel().bytesReceivedProperty());
        remainingLabel.visibleProperty().bind(mConnection.getModel().timeLeftProperty().isNotNull());
        timeLeftLabel.visibleProperty().bind(mConnection.getModel().timeLeftProperty().isNotNull());
        timeLeftLabel.textProperty().bind(mConnection.getModel().timeLeftProperty());

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
        longestShot.setCellFactory(new ColumnFormatter<>(new DecimalFormat("0.00")));
        playedHours.setCellValueFactory(new PropertyValueFactory<>("playhours"));
        location.setCellValueFactory(new PropertyValueFactory<>("location"));
        if (mConnection.ipLookupNotAvailable())
        {
            location.setVisible(false);
        }
        if (mConnection.isSteamNotAvailable())
        {
            playedHours.setVisible(false);
        }

        playersTable.setItems(mConnection.getModel().getPlayers());

        playersTable.setRowFactory(
                tableView -> {
                    final TableRow<Player> row = new TableRow<>();
                    final ContextMenu rowMenu = new ContextMenu();
                    MenuItem profileItem = new MenuItem("Profile ...");
                    profileItem.setOnAction(event -> SE4AdminGUI.showDocument("https://steamcommunity.com/profiles/" + row.getItem().getSteamId()));
                    MenuItem kickItem = new MenuItem("Kick ...");
                    kickItem.setOnAction(event -> {
                        final Player toKick = row.getItem();
                        final List<String> reasons = kickBanReasons.getReasons();
                        ChoiceDialog<String> dialog = new ChoiceDialog<>(reasons.get(0), reasons);
                        dialog.setTitle("Kick " + toKick.getName());
                        dialog.setHeaderText("Select reason for kicking " + toKick.getName());
                        dialog.setContentText("Reason:");
                        if (SE4AdminGUI.isDarkTheme())
                            dialog.getDialogPane().getStylesheets().add("dark-theme.css");

                        Optional<String> result = dialog.showAndWait();
                        result.ifPresent(r -> mConnection.kickPlayer(toKick, r));
                    });
                    MenuItem banItem = new MenuItem("Ban ...");
                    banItem.setOnAction(event -> {
                        final Player toBan = row.getItem();
                        final List<String> reasons = kickBanReasons.getReasons();
                        ChoiceDialog<String> dialog = new ChoiceDialog<>(reasons.get(0), reasons);
                        dialog.setTitle("Ban " + toBan.getName());
                        dialog.setHeaderText("Select reason for banning " + toBan.getName());
                        dialog.setContentText("Reason:");
                        if (SE4AdminGUI.isDarkTheme())
                            dialog.getDialogPane().getStylesheets().add("dark-theme.css");

                        Optional<String> result = dialog.showAndWait();
                        result.ifPresent(r -> mConnection.banPlayer(toBan, r));
                    });
                    MenuItem copyNameItem = new MenuItem("Copy name ...");
                    copyNameItem.setOnAction(event -> {
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(row.getItem().getName());
                        clipboard.setContent(content);
                    });
                    MenuItem copySteamIDItem = new MenuItem("Copy steam ID ...");
                    copySteamIDItem.setOnAction(event -> {
                        final Clipboard clipboard = Clipboard.getSystemClipboard();
                        final ClipboardContent content = new ClipboardContent();
                        content.putString(row.getItem().getSteamId());
                        clipboard.setContent(content);
                    });
                    rowMenu.getItems().addAll(profileItem, kickItem, banItem, copyNameItem, copySteamIDItem);

                    // only display context menu for non-null items:
                    row.contextMenuProperty().bind(
                            Bindings.when(Bindings.isNotNull(row.itemProperty()))
                                    .then(rowMenu)
                                    .otherwise((ContextMenu) null));
                    return row;
                });

        initializeLogWindow();
        initializeCommandsWindow();
    }

    /**
     * Initialize the log window section with pop-up menu, clear button, coloured text and auto-scroll
     */
    private void initializeLogWindow()
    {
        final ContextMenu logMenu = new ContextMenu();
        MenuItem copyAllItem = new MenuItem("Copy ALL to clipboard ...");
        copyAllItem.setOnAction(event -> {
            final Clipboard clipboard = Clipboard.getSystemClipboard();
            final ClipboardContent content = new ClipboardContent();
            final StringBuilder sb = new StringBuilder(4096);
            mConnection.getModel().getRawLogLines().forEach(line ->
            {
                sb.append(line);
                sb.append('\n');
            });
            content.putString(sb.toString());
            clipboard.setContent(content);
        });
        MenuItem copySelectedItem = new MenuItem("Copy selected to clipboard ...");
        copySelectedItem.setOnAction(event -> {
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
        });
        copySelectedItem.disableProperty().bind(logList.getSelectionModel().selectedItemProperty().isNull());
        MenuItem clearItem = new MenuItem("Clear log");
        clearItem.setOnAction(event -> mConnection.getModel().clearLog());
        logMenu.getItems().addAll(copyAllItem, copySelectedItem, clearItem);
        logList.setItems(mConnection.getModel().getLogLines());
        logList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        logList.setContextMenu(logMenu);
        logList.setCellFactory(param -> new ColouredCell(mConnection));
    }
    /**
     * Initialise send command text box and button
     */
    private void initializeCommandsWindow()
    {
        final ContextMenu commandsMenu = new ContextMenu();
        final Map<String, Menu> childMenus = new HashMap<>();
        S_COMMANDS.forEach(command -> {
            String parentName = null;
            String itemName;

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
            item.setOnAction(event -> insertCommand(command));

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

    /**
     * Send message command used by fxml file.
     */
    public void sendMessage()
    {
        if (StringUtils.isNotBlank(commandText.getText()))
        {
            logList.scrollTo(logList.getItems().size() - 1);
            mConnection.sendCommand(commandText.getText());
            commandText.clear();
        }
    }
}
