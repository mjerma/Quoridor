package hr.algebra.network;

import hr.algebra.controller.GameScreenController;
import hr.algebra.model.BoardState;
import hr.algebra.model.Player;
import hr.algebra.model.Tile;
import hr.algebra.model.Wall;
import javafx.application.Platform;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread {

    private static final String PROPERTIES_FILE = "socket.properties";
    private static final String CLIENT_PORT = "CLIENT_PORT";
    private static final Properties PROPERTIES = new Properties();

    private final GameScreenController controller;
    private final ObjectOutputStream oos;
    private final ObjectInputStream ois;

    static {
        try {
            PROPERTIES.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ClientThread(GameScreenController controller) throws IOException {
        this.controller = controller;
        InetAddress host = InetAddress.getLocalHost();
        Socket socket = new Socket(host.getHostName(), Integer.valueOf(PROPERTIES.getProperty(CLIENT_PORT)));
        oos = new ObjectOutputStream(socket.getOutputStream());
        ois = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Object object) {
        try {
            oos.writeObject(object);
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object receivedObject = ois.readObject();

                if (receivedObject.getClass() == String.class) {
                    if (receivedObject.toString().equals("switch")) {
                        Platform.runLater(
                                () -> {
                                    controller.setClientPlayerColor();
                                    controller.sendBoardState();
                                    controller.switchPlayer();
                                }
                        );
                    }
                    else if (receivedObject.toString().equals("clock")) {
                        Platform.runLater(
                                () -> {
                                    controller.startClockThread();
                                }
                        );
                    }
                }
                else {
                    BoardState state = (BoardState) receivedObject;
                    Platform.runLater(
                            () -> {
                                controller.load(state);
                            }
                    );
                }
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
