package com.tododo.controller;

import com.tododo.db.UserDAO;
import com.tododo.model.User;
import com.tododo.model.Main;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;

public class RegisterController {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    private Main mainApp;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    private void handleRegister() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            // Show error
            return;
        }

        User newUser = new User(username, email, password);
        if (UserDAO.registerUser(newUser)) {
            mainApp.showLoginView();
        } else {
            // Show error
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Registration Failed");
            alert.setHeaderText("Could not register user. Username or email may already exist.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleLoginLink() {
        mainApp.showLoginView();
    }
}