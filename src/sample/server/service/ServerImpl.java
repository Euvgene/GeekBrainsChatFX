package sample.server.service;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sample.server.handler.ClientHandler;
import sample.server.inter.AuthService;
import sample.server.inter.Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class ServerImpl implements Server {
    public List<ClientHandler> clients;
    private AuthService authService;
    public static final Logger LOGGER = LogManager.getLogger(ServerImpl.class);

    public ServerImpl() throws SQLException {
        try {
            ServerSocket serverSocket = new ServerSocket(5115);
            authService = new AuthServiceImpl();
            authService.start();
            clients = new LinkedList<>();
            while (true) {
                LOGGER.log(Level.INFO, "Wait join clients");
                Socket socket = serverSocket.accept();
                LOGGER.log(Level.INFO, "Client join");
                new ClientHandler(this, socket);
            }
        } catch (IOException e) {
            LOGGER.log(Level.ERROR, "Client join error", e);
        } finally {
            if (authService != null) {
                authService.stop();
            }
        }
    }

    @Override
    public synchronized boolean isNickBusy(String nick) {
        for (ClientHandler c : clients) {
            if (c.getNick() != null && c.getNick().equals(nick)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public synchronized void broadcastMsg(String msg) {
        for (ClientHandler c : clients) {
            c.sendMsg(msg);
        }
    }

    @Override
    public synchronized void subscribe(ClientHandler client) {
        clients.add(client);
        broadcastClientsList();
    }

    @Override
    public synchronized void unsubscribe(ClientHandler client) {
        clients.remove(client);
        if (!client.getNick().equals("")) {
            broadcastClientsList();
        }
    }

    public synchronized void broadcastClientsList() {
        StringBuilder sb = new StringBuilder("/clients ");
        for (ClientHandler o : clients) {
            sb.append(o.getNick() + " ");
        }
        LOGGER.log(Level.INFO, sb.toString());
        broadcastMsg(sb.toString());
    }

    @Override
    public AuthService getAuthService() {
        return authService;
    }
}