package sample;

import sample.server.service.ServerImpl;

import java.sql.SQLException;

public class ChatServer {
    public static void main(String[] args) throws SQLException {
        new ServerImpl();
    }
}

