package sample.server.service;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import sample.Connection.DBconn;
import sample.server.inter.AuthService;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

public class AuthServiceImpl implements AuthService {
    public List<UserEntity> usersList;
    private PreparedStatement ps;
    public static final Logger LOGGER = LogManager.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl() throws SQLException {
        usersList = new LinkedList<>();
        ps = DBconn.getInstance()
                .connection()
                .prepareStatement("SELECT * FROM users");
        ResultSet set = ps.executeQuery();
        while (set.next()) {
            UserEntity userEntity = new UserEntity();
            userEntity.setLogin(set.getString("login"));
            userEntity.setPassword(set.getString("password"));
            userEntity.setNick(set.getString("nick"));
            usersList.add(userEntity);
        }
    }

    @Override
    public void start() {
        LOGGER.log(Level.INFO, "Server is run");
    }

    @Override
    public String getNick(String login, String password) {
        for (UserEntity u : usersList) {
            if (u.login.equals(login) && u.password.equals(password)) {
                return u.nick;
            }

        }
        return null;
    }

    @Override
    public void stop() {
        LOGGER.log(Level.INFO, "Service auth stop");
    }

    public class UserEntity {
        private String login;
        private String password;
        private String nick;

        public void setLogin(String login) {
            this.login = login;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public void setNick(String nick) {
            this.nick = nick;
        }

        public UserEntity() {
        }
    }
}