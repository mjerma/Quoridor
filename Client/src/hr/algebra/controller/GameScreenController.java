package hr.algebra.controller;

import hr.algebra.model.*;
import hr.algebra.network.ClientThread;
import hr.algebra.rmi.ChatClient;
import hr.algebra.threads.ClockThread;
import hr.algebra.utils.*;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GameScreenController implements Initializable {

    @FXML
    private Label lblClock;
    @FXML
    private Label lblBluePlayerWalls;
    @FXML
    private Label lblRedPlayerWalls;
    @FXML
    private Label lblPlayer;
    @FXML
    private Button btnReplay;
    @FXML
    private Pane pnlBoard;
    @FXML
    private Button btnSaveState;
    @FXML
    private Button btnLoadState;
    @FXML
    private Button btnSendMsg;
    @FXML
    private ScrollPane spContainer;
    @FXML
    private VBox vbMessages;
    @FXML
    private TextField tfMessage;

    public static final int TILE_SIZE = 60;
    public static final int WIDTH = 9;
    public static final int HEIGHT = 9;

    private static int timeElapsed = 0;
    private int replayIndex;

    private ClientThread clientThread;
    private ClockThread clockThread;

    private Tile[][] tiles = new Tile[WIDTH][HEIGHT];
    private Wall[][] verticalWalls = new Wall[WIDTH + 1][HEIGHT];
    private Wall[][] horizontalWalls = new Wall[WIDTH][HEIGHT + 1];
    private ArrayList<Player> players = new ArrayList<>();
    private ArrayList<BoardState> states = new ArrayList<>();
    private ArrayList<BoardState> replayStates = new ArrayList<>();
    private Player currentPlayer;
    private PlayerColor clientPlayerColor;
    private boolean multiplayer = false;
    private boolean gameEnded = false;

    private static final String CLASSES_PATH = "src/hr/algebra/model";
    private static final String CLASSES_PACKAGE = "hr.algebra.model.";

    private ObservableList<Node> messages;
    private ChatClient chatClient;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        createContent();
    }

    public void initClientThread() throws IOException {
        clientThread = new ClientThread(this);
        clientThread.setDaemon(true);
        clientThread.start();
        multiplayer = true;
        btnSaveState.setDisable(true);
        btnLoadState.setDisable(true);
        btnReplay.setDisable(true);
        tfMessage.setDisable(false);
        btnSendMsg.setDisable(false);
        chatClient = new ChatClient(this);
        messages = FXCollections.observableArrayList();
        Bindings.bindContentBidirectional(messages, vbMessages.getChildren());
    }

    public void startClockThread() {
        clockThread = new ClockThread(this);
        clockThread.start();
    }

    private void createContent() {
        pnlBoard.setPrefSize(WIDTH * TILE_SIZE + TILE_SIZE / 12, HEIGHT * TILE_SIZE + TILE_SIZE / 12);
        tfMessage.setDisable(true);
        btnSendMsg.setDisable(true);
        addBoardContent();
    }

    // Add tiles, walls and players to pane
    private void addBoardContent() {
        for (int y = 0; y <= HEIGHT; y++) {
            for (int x = 0; x <= WIDTH; x++) {
                if (y < HEIGHT) {
                    if (x < WIDTH) {
                        Tile tile = new Tile(x, y);
                        tiles[x][y] = tile;
                        pnlBoard.getChildren().add(tile);
                    }
                    Wall verticalWall = new Wall(x, y, true);
                    verticalWalls[x][y] = verticalWall;
                    pnlBoard.getChildren().add(verticalWall);
                }
                if (x == WIDTH) {
                    continue;
                }
                Wall horizontalWall = new Wall(x, y, false);
                horizontalWalls[x][y] = horizontalWall;
                pnlBoard.getChildren().add(horizontalWall);
            }
        }
        Player player;
        int playerX = Math.round(WIDTH / 2);
        int playerY = 0;

        // Create Red player
        player = createPlayer(playerX, playerY, PlayerColor.RED);
        tiles[playerX][playerY].setPlayer(player);
        players.add(player);
        pnlBoard.getChildren().add(player);
        lblRedPlayerWalls.setText("Red player walls: " + player.getWallCount());

        // Create Blue player
        playerY = HEIGHT - 1;
        player = createPlayer(playerX, playerY, PlayerColor.BLUE);
        tiles[playerX][playerY].setPlayer(player);
        players.add(player);
        pnlBoard.getChildren().add(player);
        lblBluePlayerWalls.setText("Blue player walls: " + player.getWallCount());

        addWallEventHandlers();

        currentPlayer = players.get(new Random().nextInt(players.size()));
        currentPlayer.highlight();
    }

    private void addWallEventHandlers() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                final int currentX = x;
                final int currentY = y;
                final int nextHWallX = x + 1;
                final int nextHWallY = y;
                final int nextVWallX = x;
                final int nextVWallY = y + 1;

                Wall horizontalWall = horizontalWalls[x][y];
                horizontalWall.setOnMouseEntered(e -> {
                    if (!horizontalWall.isPlaced()
                            && currentY > 0
                            && nextHWallX < WIDTH
                            && !horizontalWalls[nextHWallX][nextHWallY].isPlaced()
                            && !(verticalWalls[currentX + 1][currentY - 1].isPlaced()
                            && verticalWalls[currentX + 1][currentY].isPlaced())) {
                        horizontalWall.setFill(Color.valueOf("#ffc47d"));
                        horizontalWalls[nextHWallX][nextHWallY].setFill(Color.valueOf("#ffc47d"));
                    }
                });
                horizontalWall.setOnMouseExited(e -> {
                    if (!horizontalWall.isPlaced()
                            && currentY > 0
                            && nextHWallX < WIDTH
                            && !horizontalWalls[nextHWallX][nextHWallY].isPlaced()
                            && !(verticalWalls[currentX + 1][currentY - 1].isPlaced()
                            && verticalWalls[currentX + 1][currentY].isPlaced())) {
                        horizontalWall.setFill(Color.valueOf("#000"));
                        horizontalWalls[nextHWallX][nextHWallY].setFill(Color.valueOf("#000"));
                    }
                });
                horizontalWall.setOnMousePressed(e -> {
                    if (currentPlayer.getWallCount() > 0) {
                        if (!horizontalWall.isPlaced()
                                && currentY > 0
                                && nextHWallX < WIDTH
                                && !horizontalWalls[nextHWallX][nextHWallY].isPlaced()
                                && !(verticalWalls[currentX + 1][currentY - 1].isPlaced()
                                && verticalWalls[currentX + 1][currentY].isPlaced())) {
                            horizontalWall.placeWall(players.get(1));
                            horizontalWalls[nextHWallX][nextHWallY].placeWall(players.get(1));
                            currentPlayer.setWallCount(currentPlayer.getWallCount() - 1);
                            changeWallCount();
                            sendBoardState();
                            switchPlayer();
                        }
                    } else {
                        MessageUtils.showAlert("You have no walls left!", "Alert");
                    }
                });

                Wall verticalWall = verticalWalls[x][y];
                verticalWall.setOnMouseEntered(e -> {
                    if (!verticalWall.isPlaced()
                            && currentX > 0
                            && nextVWallY < HEIGHT
                            && !verticalWalls[nextVWallX][nextVWallY].isPlaced()
                            && !(horizontalWalls[currentX - 1][currentY + 1].isPlaced()
                            && horizontalWalls[currentX][currentY + 1].isPlaced())) {
                        verticalWall.setFill(Color.valueOf("#ffc47d"));
                        verticalWalls[nextVWallX][nextVWallY].setFill(Color.valueOf("#ffc47d"));
                    }
                });

                verticalWall.setOnMouseExited(e -> {
                    if (!verticalWall.isPlaced()
                            && currentX > 0
                            && nextVWallY < HEIGHT
                            && !verticalWalls[nextVWallX][nextVWallY].isPlaced()
                            && !(horizontalWalls[currentX - 1][currentY + 1].isPlaced()
                            && horizontalWalls[currentX][currentY + 1].isPlaced())) {
                        verticalWall.setFill(Color.valueOf("#000"));
                        verticalWalls[nextVWallX][nextVWallY].setFill(Color.valueOf("#000"));
                    }
                });
                verticalWall.setOnMousePressed(e -> {
                    if (currentPlayer.getWallCount() > 0) {
                        if (!verticalWall.isPlaced()
                                && currentX > 0
                                && nextVWallY < HEIGHT
                                && !verticalWalls[nextVWallX][nextVWallY].isPlaced()
                                && !(horizontalWalls[currentX - 1][currentY + 1].isPlaced()
                                && horizontalWalls[currentX][currentY + 1].isPlaced())) {
                            verticalWall.placeWall(players.get(0));
                            verticalWalls[nextVWallX][nextVWallY].placeWall(players.get(1));
                            currentPlayer.setWallCount(currentPlayer.getWallCount() - 1);
                            changeWallCount();
                            sendBoardState();
                            switchPlayer();
                        }
                    } else {
                        MessageUtils.showAlert("You have no walls left!", "Alert");
                    }
                });
            }
        }
    }

    private Player createPlayer(int x, int y, PlayerColor color) {
        Player player = new Player(x, y, color);
        addMouseEventHandler(player);
        return player;
    }

    private void addMouseEventHandler(Player player) {
        player.setOnMouseReleased(e -> {

            if (currentPlayer != player) {
                player.undoMove();
                MessageUtils.showAlert("Current player is: " + currentPlayer.getColor(), "Wrong player!");
                return;
            }

            int currentX = convertToBoardCoordinate(player.getCurrentX());
            int currentY = convertToBoardCoordinate(player.getCurrentY());
            int mouseNewX = convertToBoardCoordinate(player.getLayoutX());
            int mouseNewY = convertToBoardCoordinate(player.getLayoutY());

            MoveType result = checkMove(currentX, currentY, mouseNewX, mouseNewY);

            switch (result) {
                case NONE:
                    player.undoMove();
                    break;
                case STANDARD:
                    player.move(mouseNewX, mouseNewY);
                    tiles[currentX][currentY].setPlayer(null);
                    tiles[mouseNewX][mouseNewY].setPlayer(player);
                    checkWin(player);
                    sendBoardState();
                    switchPlayer();
            }
        });
    }

    private MoveType checkMove(int currentX, int currentY, int mouseNewX, int mouseNewY) {

        //Moving off board
        if (mouseNewX < 0 || mouseNewY < 0 || mouseNewX >= WIDTH || mouseNewY >= HEIGHT) {
            return MoveType.NONE;
        }

        //Moving in x or y direction
        if (mouseNewY - currentY == 0 || mouseNewX - currentX == 0) {
            if (tiles[mouseNewX][mouseNewY].hasPlayer()) {
                return MoveType.NONE;
            }
            //Moving by one tile
            //up
            if (mouseNewY - currentY == -1) {
                if (!horizontalWalls[currentX][currentY].isPlaced()) {
                    return MoveType.STANDARD;
                }
            }
            //down
            if (mouseNewY - currentY == 1) {
                if (!horizontalWalls[currentX][currentY + 1].isPlaced()) {
                    return MoveType.STANDARD;
                }
            }
            //left
            if (mouseNewX - currentX == -1) {
                if (!verticalWalls[currentX][currentY].isPlaced()) {
                    return MoveType.STANDARD;
                }
            }
            //right
            if (mouseNewX - currentX == 1) {
                if (!verticalWalls[currentX + 1][currentY].isPlaced()) {
                    return MoveType.STANDARD;
                }
            }
            //Moving by two tiles
            //up
            if (mouseNewY - currentY == -2 && tiles[currentX][currentY - 1].hasPlayer()) {
                if (!horizontalWalls[currentX][currentY - 1].isPlaced()
                        && !horizontalWalls[currentX][currentY].isPlaced()) {
                    return MoveType.STANDARD;
                }
            }
            //down
            if (mouseNewY - currentY == 2 && tiles[currentX][currentY + 1].hasPlayer()) {
                if (!horizontalWalls[currentX][currentY + 2].isPlaced()
                        && !horizontalWalls[currentX][currentY + 1].isPlaced()) {
                    return MoveType.STANDARD;
                }
            }
            //left
            if (mouseNewX - currentX == -2 && tiles[currentX - 1][currentY].hasPlayer()) {
                if (!verticalWalls[currentX - 1][currentY].isPlaced()
                        && !verticalWalls[currentX][currentY].isPlaced()) {
                    return MoveType.STANDARD;
                }
            }
            //right
            if (mouseNewX - currentX == 2 && tiles[currentX + 1][currentY].hasPlayer()) {
                if (!verticalWalls[currentX + 2][currentY].isPlaced()
                        && !verticalWalls[currentX + 1][currentY].isPlaced()) {
                    return MoveType.STANDARD;
                }
            }
        }
        //moving diagonally
        //up right
        if (mouseNewX - currentX == 1 && mouseNewY - currentY == -1 && tiles[currentX][currentY - 1].hasPlayer()) {
            if (horizontalWalls[currentX][currentY - 1].isPlaced()
                    && !horizontalWalls[currentX][currentY].isPlaced()) {
                return MoveType.STANDARD;
            }
        }
        //up left
        if (mouseNewX - currentX == -1 && mouseNewY - currentY == -1 && tiles[currentX][currentY - 1].hasPlayer()) {
            if (horizontalWalls[currentX][currentY - 1].isPlaced()
                    && !horizontalWalls[currentX][currentY].isPlaced()) {
                return MoveType.STANDARD;
            }
        }
        //down right
        if (mouseNewX - currentX == 1 && mouseNewY - currentY == 1 && tiles[currentX][currentY + 1].hasPlayer()) {
            if (horizontalWalls[currentX][currentY + 2].isPlaced()
                    && !horizontalWalls[currentX][currentY + 1].isPlaced()) {
                return MoveType.STANDARD;
            }
        }
        //down left
        if (mouseNewX - currentX == -1 && mouseNewY - currentY == 1 && tiles[currentX][currentY + 1].hasPlayer()) {
            if (horizontalWalls[currentX][currentY + 2].isPlaced()
                    && !horizontalWalls[currentX][currentY + 1].isPlaced()) {
                return MoveType.STANDARD;
            }
        }
        //right up
        if (mouseNewX - currentX == 1 && mouseNewY - currentY == -1 && tiles[currentX + 1][currentY].hasPlayer()) {
            if (verticalWalls[currentX + 2][currentY].isPlaced()
                    && !verticalWalls[currentX + 1][currentY].isPlaced()) {
                return MoveType.STANDARD;
            }
        }
        //right down
        if (mouseNewX - currentX == 1 && mouseNewY - currentY == 1 && tiles[currentX + 1][currentY].hasPlayer()) {
            if (verticalWalls[currentX + 2][currentY].isPlaced()
                    && !verticalWalls[currentX + 1][currentY].isPlaced()) {
                return MoveType.STANDARD;
            }
        }
        //left up
        if (mouseNewX - currentX == -1 && mouseNewY - currentY == -1 && tiles[currentX - 1][currentY].hasPlayer()) {
            if (verticalWalls[currentX - 1][currentY].isPlaced()
                    && !verticalWalls[currentX][currentY].isPlaced()) {
                return MoveType.STANDARD;
            }
        }
        //left down
        if (mouseNewX - currentX == -1 && mouseNewY - currentY == 1 && tiles[currentX - 1][currentY].hasPlayer()) {
            if (verticalWalls[currentX - 1][currentY].isPlaced()
                    && !verticalWalls[currentX][currentY].isPlaced()) {
                return MoveType.STANDARD;
            }
        }
        return MoveType.NONE;
    }

    private int convertToBoardCoordinate(double pixel) {
        return (int) (pixel + TILE_SIZE / 2) / TILE_SIZE;
    }

    public void switchPlayer() {
        if (!gameEnded) {
            currentPlayer.undoHighlight();
            currentPlayer = getOtherPlayer();
            currentPlayer.highlight();
        }
    }

    private void checkWin(Player player) {
        if (player.getColor() == PlayerColor.RED
                && convertToBoardCoordinate(player.getCurrentY()) == HEIGHT - 1) {
            MessageUtils.showAlert("Red player won!", "Game ended");
            pnlBoard.setDisable(true);
            gameEnded = true;
        }
        if (player.getColor() == PlayerColor.BLUE
                && convertToBoardCoordinate(player.getCurrentY()) == 0) {
            MessageUtils.showAlert("Blue player won!", "Game ended");
            pnlBoard.setDisable(true);
            gameEnded = true;
        }
    }

    public void load(BoardState state) {
        tiles = state.getTiles();
        horizontalWalls = state.getHorizontalWalls();
        verticalWalls = state.getVerticalWalls();
        players = state.getPlayers();
        currentPlayer = state.getCurrentPlayer();

        pnlBoard.getChildren().clear();

        setupTiles();
        setupWalls(HEIGHT, WIDTH + 1, verticalWalls);
        setupWalls(HEIGHT + 1, WIDTH, horizontalWalls);
        addWallEventHandlers();

        players.forEach(p -> {
            p.setupScene(convertToBoardCoordinate(p.getCurrentX()), convertToBoardCoordinate(p.getCurrentY()), p.getColor());
            addMouseEventHandler(p);
            pnlBoard.getChildren().add(p);
        });
        currentPlayer.highlight();

        if (multiplayer) {
            pnlBoard.setDisable(false);
            states.add(deepCopyBoardState());
        } else {
            timeElapsed = state.getTimeElapsed();
        }

        checkWin(currentPlayer);
        changeWallCount();
        switchPlayer();

        if (clientPlayerColor == null && multiplayer) {
            setClientPlayerColor();
        }
    }

    private void changeWallCount() {
        Player otherPlayer = getOtherPlayer();
        if (currentPlayer.getColor() == PlayerColor.RED) {
            lblRedPlayerWalls.setText("Red player walls: " + currentPlayer.getWallCount());
            lblBluePlayerWalls.setText("Blue player walls: " + otherPlayer.getWallCount());
        } else {
            lblRedPlayerWalls.setText("Red player walls: " + otherPlayer.getWallCount());
            lblBluePlayerWalls.setText("Blue player walls: " + currentPlayer.getWallCount());
        }
    }

    private Player getOtherPlayer() {
        return players.stream().filter(p -> p.getColor() != currentPlayer.getColor()).findFirst().get();
    }

    public void sendBoardState() {
        BoardState state = deepCopyBoardState();
        states.add(state);
        if (multiplayer) {
            clientThread.send(state);
            pnlBoard.setDisable(true);
        }
    }

    public void serialize() {
        try {
            File file = FileUtils.saveFileDialog(btnSaveState.getScene().getWindow(), "ser");
            if (file != null) {
                BoardState state = deepCopyBoardState();
                SerializationUtils.write(state, file.getAbsolutePath());
            }
            MessageUtils.showAlert("Data successfully saved in file", "Saved");
        } catch (IOException ex) {
            MessageUtils.showError("Could not save data", "Save Error");
            Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void deserialize() {
        File file = FileUtils.uploadFileDialog(btnLoadState.getScene().getWindow(), "ser");
        if (file != null) {
            try {
                BoardState state = (BoardState) SerializationUtils.read(file.getAbsolutePath());
                load(state);
                switchPlayer();
            } catch (IOException | ClassNotFoundException ex) {
                MessageUtils.showError("Could not load data", "Load Error");
                Logger.getLogger(GameScreenController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void generateDocumentation() {
        File doc = new File("documentation.html");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(doc));
             DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(CLASSES_PATH))) {

            StringBuilder docContent = new StringBuilder();
            docContent.append("<html>");
            docContent.append("<body>");
            docContent.append("<h1>Quoridor game documentation</h1>");
            docContent.append("</br>");
            docContent.append("\n");

            stream.forEach(file -> {
                String fileName = file.getFileName().toString();
                String className = fileName.substring(0, fileName.indexOf("."));
                try {
                    Class<?> clazz = Class.forName(CLASSES_PACKAGE.concat(className));
                    ReflectionUtils.readClassAndMembersInfo(clazz, docContent);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            });

            docContent.append("</body>");
            docContent.append("</html>");
            writer.write(docContent.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void postMessage(String message) {
        Platform.runLater(() -> addMessage(message));
    }

    private void addMessage(String message) {
        Label label = new Label();
        label.setFont(new Font(15));
        label.setText(message);
        label.setTextFill(Paint.valueOf("#ccc"));
        messages.add(label);
        moveScrollPane();
    }

    private void moveScrollPane() {
        spContainer.applyCss();
        spContainer.layout();
        spContainer.setVvalue(1D);
    }

    @FXML
    private void onEnterPress(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            sendChatMessage();
        }
    }

    @FXML
    private void sendChatMessage() {
        if (tfMessage.getText().trim().length() > 0) {
            chatClient.sendMessage(String.format("%s: %s", clientPlayerColor.toString(), tfMessage.getText().trim()));
            tfMessage.clear();
        }
    }

    public void setClientPlayerColor() {
        clientPlayerColor = currentPlayer.getColor();
        lblPlayer.setVisible(true);
        lblPlayer.setText("Your player color: " + clientPlayerColor.toString());
    }

    @FXML
    private void saveReplay() {
        DOMUtils.saveStates(states);
    }

    @FXML
    private void replayGame() {
        try {
            replayStates = DOMUtils.loadStates();
            replayStates();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void replayStates() {
        if (replayIndex >= replayStates.size()) {
            replayIndex = 0;
            replayStates = null;
            MessageUtils.showAlert("Replay finished", "Game replay");
            return;
        }

        BoardState state = replayStates.get(replayIndex);

        PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
        pause.setOnFinished(event -> {
            replayIndex++;
            replayStates();
        });

        load(state);
        pause.play();
    }

    public void updateTime() {
        timeElapsed++;
        lblClock.setText(String.format("%02d : %02d",
                TimeUnit.SECONDS.toMinutes(timeElapsed),
                TimeUnit.SECONDS.toSeconds(timeElapsed) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(timeElapsed))
        ));
    }

    public boolean hasGameEnded() {
        return gameEnded;
    }

    private void setupTiles() {
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                tiles[x][y].setupScene();
                pnlBoard.getChildren().add(tiles[x][y]);
            }
        }
    }

    private void setupWalls(int height, int width, Wall[][] walls) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                walls[x][y].setupScene();
                pnlBoard.getChildren().add(walls[x][y]);
            }
        }
    }

    public BoardState deepCopyBoardState() {
        Tile[][] tilesCopy = deepCopyTiles(tiles);
        Wall[][] verticalWallsCopy = deepCopyWalls(verticalWalls);
        Wall[][] horizontalWallsCopy = deepCopyWalls(horizontalWalls);
        ArrayList<Player> playersCopy = deepCopyPlayers(players);
        Player currentPlayerCopy = new Player(currentPlayer.getCurrentX(), currentPlayer.getCurrentY(),
                currentPlayer.getColor(), currentPlayer.getWallCount());

        return new BoardState(tilesCopy, horizontalWallsCopy, verticalWallsCopy, playersCopy, currentPlayerCopy, timeElapsed);
    }

    private Tile[][] deepCopyTiles(Tile[][] tileArray) {
        Tile[][] tilesCopy = new Tile[tileArray.length][];

        for (int i = 0; i < tileArray.length; i++) {
            tilesCopy[i] = new Tile[tileArray[i].length];
            for (int j = 0; j < tileArray[i].length; j++) {
                Tile tempTile = tileArray[i][j];
                Player tempPlayer = null;
                if (tempTile.getPlayer() != null) {
                    Player tilePlayer = tempTile.getPlayer();
                    tempPlayer = new Player(tilePlayer.getCurrentX(), tilePlayer.getCurrentY(),
                            tilePlayer.getColor(), tilePlayer.getWallCount());
                }
                tilesCopy[i][j] = new Tile(tempTile.getXCoordinate(), tempTile.getYCoordinate(), tempPlayer);
            }
        }
        return tilesCopy;
    }

    private Wall[][] deepCopyWalls(Wall[][] wallArray) {
        Wall[][] wallsCopy = new Wall[wallArray.length][];

        for (int i = 0; i < wallArray.length; i++) {
            wallsCopy[i] = new Wall[wallArray[i].length];
            for (int j = 0; j < wallArray[i].length; j++) {
                Wall temp = wallArray[i][j];
                wallsCopy[i][j] = new Wall(temp.getXCoordinate(), temp.getYCoordinate(),
                        temp.isVertical(), temp.getPlayer());
            }
        }
        return wallsCopy;
    }

    private ArrayList<Player> deepCopyPlayers(ArrayList<Player> players) {
        ArrayList<Player> playersCopy = new ArrayList<>();

        players.forEach(p -> playersCopy.add(new Player(p.getCurrentX(), p.getCurrentY(),
                p.getColor(), p.getWallCount())));
        return playersCopy;
    }
}
