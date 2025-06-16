package com.tododo.controller;

import com.tododo.db.UserDAO;
import com.tododo.model.User;
import com.tododo.model.Main;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class LoginController {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        User user = UserDAO.loginUser(username, password);

        if (user != null) {
            mainApp.showTaskView(user);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Failed");
            alert.setHeaderText("Invalid username or password.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleRegisterLink() {
        mainApp.showRegisterView();
    }
}