package sample.client;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class Controller {
    @FXML
    private TextField textField;

    @FXML
    private TextArea textArea;

    @FXML
    HBox chatPanel;

    @FXML
    HBox authPanel;

    @FXML
    TextField loginfield;

    @FXML
    PasswordField passwordfiled;

    Socket socket;
    DataInputStream dis;
    DataOutputStream dos;
    private int timeOut = 120000;
    private final String IP_ADRESS = "localhost";
    private final int PORT = 5115;

    private void setAuthorized(boolean isAuthorized) {
        if (!isAuthorized) {
            authPanel.setVisible(true);
            authPanel.setManaged(true);
            chatPanel.setVisible(false);
            chatPanel.setManaged(false);
        } else {
            authPanel.setVisible(false);
            authPanel.setManaged(false);
            textArea.clear();
            chatPanel.setVisible(true);
            chatPanel.setManaged(true);
        }
    }

    public void start() {
        try {
            setAuthorized(false);
            socket = new Socket(IP_ADRESS, PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            Thread t1 = new Thread(() -> {
                try {
                    long countTime = System.currentTimeMillis();
                    while ((System.currentTimeMillis() - countTime) <= timeOut) {
                        String strMsg = dis.readUTF();
                        if (strMsg.startsWith("/authOk")) {
                            setAuthorized(true);
                            socket.setSoTimeout(0);
                            break;
                        } else if (authPanel.isVisible()) {
                            socket.setSoTimeout(timeOut - (int) (System.currentTimeMillis() - countTime));

                        }
                        textArea.appendText(strMsg + "\n");
                    }
                    while (true) {
                        String strMsg = dis.readUTF();
                        if (strMsg.equals("/exit")) {
                            break;
                        }
                        textArea.appendText(strMsg + "\n");
                    }
                } catch (IOException | ArrayIndexOutOfBoundsException e) {
                    System.out.println("disconnected");
                } finally {
                    try {
                        setAuthorized(false);
                        socket.close();
                        textArea.clear();
                    } catch (IOException e) {
                        System.out.println("disconnected");
                    }
                }
            });
            t1.setDaemon(true);
            t1.start();
        } catch (IOException e) {
            textArea.appendText("Server is not working!" + "\n");
        }
    }

    public void tryToAuth() {
        if (socket == null || socket.isClosed()) {
            start();
        }
        try {
            dos.writeUTF("/auth " + loginfield.getText() + " " + passwordfiled.getText());
            loginfield.clear();
            passwordfiled.clear();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMsg() {
        try {
            if (!textField.getText().trim().equals("")) {
                dos.writeUTF(textField.getText());
                textField.clear();
                textField.requestFocus();
            } else {
                textField.clear();
            }
        } catch (IOException e) {
            textField.clear();
            textArea.appendText("Can't send a message." + "\n");
        }
    }

    @FXML
    void disableTextInput(KeyEvent event) {
        textArea.editableProperty().setValue(false);
    }

    @FXML
    public void mouseClickedTextArea(MouseEvent mouseEvent) {
        textArea.editableProperty().setValue(false);
    }
}

