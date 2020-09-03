package sample.server.service;

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
        System.out.println("Server is run");
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
        System.out.println("Сервис аутентификации остановлен");

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

        public UserEntity(String login, String password, String nick) {
            this.login = login;
            this.password = password;
            this.nick = nick;
        }

        public UserEntity() {
        }
      }
}