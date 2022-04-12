/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hr.algebra.rmi;

import hr.algebra.ServerApp;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class ChatServer{

    private static final String RMI_CLIENT = "client";
    private static final String RMI_SERVER = "server";
    private static final int REMOTE_PORT = 1099;
    private static final int RANDOM_PORT_HINT = 0;

    private ChatService server;
    private Registry registry;
    private ArrayList<ChatService> clients = new ArrayList<>();

    private final ServerApp serverApp;

    public ChatServer(ServerApp serverApp) {
        this.serverApp = serverApp;
        publishServer();
        waitForClient();
    }

    public void publishServer() {
        server = new ChatService() {
            @Override
            public void send(String message) throws RemoteException {
                for (ChatService client : clients) {
                    client.send(message);
                }
            }
        };
        // publish server
        try {
            registry = LocateRegistry.createRegistry(REMOTE_PORT);
            ChatService stub = (ChatService) UnicastRemoteObject.exportObject(server, RANDOM_PORT_HINT);
            registry.rebind(RMI_SERVER, stub);

        } catch (RemoteException ex) {
            Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void waitForClient() {

        Thread thread = new Thread(() -> {
            while (clients.size() < 2) {
                try {
                    ChatService client = (ChatService) registry.lookup(RMI_CLIENT);
                    if(!clients.contains(client)) {
                        clients.add(client);
                    }
                } catch (RemoteException | NotBoundException ex) {
                    System.out.println("[CHAT SERVER] Waiting for chat client");
                }
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ChatServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
}
