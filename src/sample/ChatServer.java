package sample;

import sample.Connection.DBconn;
import sample.server.service.ServerImpl;

import java.sql.SQLException;

public class ChatServer {
    public static void main(String[] args) throws SQLException {
        System.out.println(DBconn.getInstance().connection().prepareStatement("SHOW TABLES"));
        new ServerImpl();
    }
}

