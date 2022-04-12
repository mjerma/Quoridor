package hr.algebra;

import hr.algebra.network.ServerThread;
import hr.algebra.rmi.ChatServer;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerApp {

    private static final String PROPERTIES_FILE = "socket.properties";
    private static final String CLIENT_PORT = "CLIENT_PORT";
    private static final Properties PROPERTIES = new Properties();

    private static final ArrayList<ServerThread> clients = new ArrayList<>();

    private ChatServer chatServer;

    static {
        try {
            PROPERTIES.load(new FileInputStream(PROPERTIES_FILE));
        } catch (IOException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    public ServerApp() {

        try {
            ServerSocket listener = new ServerSocket(Integer.parseInt(PROPERTIES.getProperty(CLIENT_PORT)));
            chatServer = new ChatServer(this);

            while (true) {
                if (clients.size() < 2) {
                    System.out.println("[GAME SERVER] Waiting for client connection...");
                    Socket clientSocket = listener.accept();
                    System.out.println("[GAME SERVER] Connected to client!");
                    ServerThread serverThread = new ServerThread(clientSocket);
                    serverThread.setDaemon(true);
                    clients.add(serverThread);
                    serverThread.start();
                }
                else {
                    System.out.println("[GAME SERVER] Starting game");
                    randomizeFirstPlayer();
                    startClock();
                    break;
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public static void sendToOtherClient(Object object, ServerThread sender) {
        clients.forEach(client -> {
            try {
                if (!client.equals(sender)) {
                    client.sendObject(object);
                }
            } catch (IOException ex) {
                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public static void startClock() {
        clients.forEach(client -> {
            try {
                client.sendObject("clock");
            } catch (IOException ex) {
                Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    public static void randomizeFirstPlayer() {
        try {
            ServerThread randomClient = clients.get(new Random().nextInt(clients.size()));
            randomClient.sendObject("switch");
        } catch (IOException ex) {
            Logger.getLogger(ServerApp.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String[] args) {
        new ServerApp();
    }

}


















