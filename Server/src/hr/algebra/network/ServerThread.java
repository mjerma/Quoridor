package hr.algebra.network;

import hr.algebra.ServerApp;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerThread extends Thread {

    private final ObjectInputStream ois;
    private final ObjectOutputStream oos;

    public ServerThread(Socket clientSocket) throws IOException {
        ois = new ObjectInputStream(clientSocket.getInputStream());
        oos = new ObjectOutputStream(clientSocket.getOutputStream());
    }

    @Override
    public void run() {
        try {
            while (true) {
                ServerApp.sendToOtherClient(ois.readObject(), this);
            }
        } catch (SocketException ex) {
            System.out.println(ex.getMessage());
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServerThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void sendObject(Object object) throws IOException {
        oos.writeObject(object);
    }
}
