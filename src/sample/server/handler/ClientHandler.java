package sample.server.handler;

import sample.server.service.ServerImpl;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler {
    private ServerImpl server;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;

    private String nick;

    public ClientHandler(ServerImpl server, Socket socket) {
        try {
            this.server = server;
            this.socket = socket;
            this.dis = new DataInputStream(socket.getInputStream());
            this.dos = new DataOutputStream(socket.getOutputStream());
            this.nick = "";
            new Thread(() -> {
                try {
                    authentication();
                    readMessage();
                } finally {
                    closeConnection();
                }
            }).start();
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
                            server.subscribe(this);
                            return;
                        } else {
                            sendMsg("You are logged in");
                        }
                    } else {
                        sendMsg("Incorrect password or login");
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("authentication error");
        }
    }

    public void sendMsg(String msg) {
        try {
            dos.writeUTF(msg);
        } catch (IOException e) {
            System.out.println(nick + " close chat and disconnect");
        }
    }

    public void readMessage() {
        try {
            while (true) {
                String clientStr = dis.readUTF();
                if (clientStr.startsWith("/")) {
                    System.out.println("from " + this.nick + ": " + clientStr);
                    if (clientStr.equals("/exit")) {
                        break;
                    } else if (clientStr.startsWith("/w") && clientStr.split("\\s").length > 2) {
                        String toUser = clientStr.split("\\s")[1];
                        String msg = clientStr.split(" ")[2];
                        privateMsg(ClientHandler.this, toUser, msg);
                    }
                } else {
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
        } else {
            for (ClientHandler c : server.clients) {
                if (toUser.equals(c.getNick())) {
                    c.sendMsg("Personal from " + fromUser.getNick() + ": " + msg);
                    break;
                }
            }
            fromUser.sendMsg("Personal to: " + toUser + " " + msg);
        }
    }

    public String getNick() {
        return nick;
    }

    private void closeConnection() {
        server.unsubscribe(this);
        if (!this.nick.equals("")) {
            server.broadcastMsg(this.nick + ": exit from chat");
        }
        try {
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            dos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

