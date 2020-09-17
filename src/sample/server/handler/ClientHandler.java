package sample.server.handler;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sample.server.service.ServerImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientHandler {
    private ServerImpl server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    public static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);

    private String nick;

    public ClientHandler(ServerImpl server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.nick = "";
            ExecutorService executorService = Executors.newCachedThreadPool();
            executorService.execute(() -> {
                try {
                    authentication();
                    readMessage();
                } finally {
                    closeConnection();
                }
            });
            executorService.shutdown();
        } catch (IOException e) {
            throw new RuntimeException("Problems with creating a client handler");
        }
    }

    private void authentication() {
        try {
            while (true) {
                String str = dis.readUTF();
                if (str.startsWith("/auth")) {
                    String[] dataArray = str.split("\\s");
                    String nick = server.getAuthService().getNick(dataArray[1], dataArray[2]);
                    if (nick != null) {
                        if (!server.isNickBusy(nick)) {
                            sendMsg("/authOk " + nick);
                            this.nick = nick;
                            server.broadcastMsg(this.nick + " join to chat");
                            LOGGER.log(Level.INFO, this.nick + " join to chat");
                            server.subscribe(this);
                            return;
                        } else {
                            sendMsg("You are logged in");
                            LOGGER.log(Level.WARN, "authentication error " + nick + " already loged in");
                        }
                    } else {
                        LOGGER.log(Level.WARN, "Try to log in with incorrect login or password");
                        sendMsg("Incorrect password or login");
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.WARN, "authentication error" + getNick(), e);
        }
    }

    public void sendMsg(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            LOGGER.log(Level.WARN, getNick() + " close chat and disconnect");
        }
    }

    public void readMessage() {
        try {
            while (true) {
                String clientStr = dis.readUTF();
                if (clientStr.startsWith("/")) {
                    if (clientStr.equals("/exit")) {
                        break;
                    } else if (clientStr.startsWith("/w") && clientStr.split("\\s").length > 2) {
                        String toUser = clientStr.split("\\s")[1];
                        String msg = "";
                        String nick = clientStr.split(" ")[1];
                        for (int i = nick.length() + 4; i < clientStr.length(); i++) {
                            msg += clientStr.split("")[i];
                        }

                        privateMsg(ClientHandler.this, toUser, msg);
                    } else {
                        LOGGER.log(Level.INFO, nick + " to all : " + clientStr);
                        server.broadcastMsg(nick + ": " + clientStr);
                    }
                } else {
                    LOGGER.log(Level.INFO, nick + " to all : " + clientStr);
                    server.broadcastMsg(nick + ": " + clientStr);
                }
            }
        } catch (IOException e) {
            sendMsg("Wrong command");
        }
    }

    public void privateMsg(ClientHandler fromUser, String toUser, String msg) {
        if (!server.isNickBusy(toUser)) {
            sendMsg(toUser + " is not conected!");
            LOGGER.log(Level.INFO, fromUser.getNick() + " try to send personal msg to " + toUser);
        } else {
            for (ClientHandler c : server.clients) {
                if (toUser.equals(c.getNick())) {
                    c.sendMsg("Personal from " + fromUser.getNick() + ": " + msg);
                    LOGGER.log(Level.INFO, "Personal from " + fromUser.getNick() + " to " + toUser + ":" + msg);
                    break;
                }
            }
            fromUser.sendMsg("Personal to " + toUser + ": " + msg);
        }
    }

    public String getNick() {
        return nick;
    }

    private void closeConnection() {
        server.unsubscribe(this);
        if (!this.nick.equals("")) {
            server.broadcastMsg(this.nick + ": exit from chat");
            LOGGER.log(Level.INFO, this.nick + "- exit from chat");
        }
        try {
            dis.close();
        } catch (IOException e) {
            LOGGER.log(Level.TRACE, e);
        }

        try {
            dos.close();
        } catch (IOException e) {
            LOGGER.log(Level.TRACE, e);
        }

        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.log(Level.TRACE, e);
        }
    }
}