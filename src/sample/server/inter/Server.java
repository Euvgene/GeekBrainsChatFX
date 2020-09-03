package sample.server.inter;

import sample.server.handler.ClientHandler;

public interface Server {
    boolean isNickBusy(String nick);

    void broadcastMsg(String msg);

    void subscribe(ClientHandler client);

    void unsubscribe(ClientHandler client);

    AuthService getAuthService();
}