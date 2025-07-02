package com.tododo.model;

import com.tododo.controller.LoginController;
import com.tododo.controller.ProfileController;
import com.tododo.controller.RegisterController;
import com.tododo.controller.TaskController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class Main extends Application {
    private Stage primaryStage;
    
    // A static variable to hold the logged-in user
    private static User loggedInUser;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("Tododo - To-Do List App");
        showLoginView();
    }

    public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Login.fxml"));
            Scene scene = new Scene(loader.load());
            LoginController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showRegisterView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Register.fxml"));
            Scene scene = new Scene(loader.load());
            RegisterController controller = loader.getController();
            controller.setMainApp(this);
            primaryStage.setScene(scene);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void showTaskView(User user) {
        loggedInUser = user;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/main.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Optionally pass the user to the TaskController if needed
            TaskController controller = loader.getController();
            // controller.setUser(user); // You would need to add this method to TaskController

            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void showProfileView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/Profile.fxml"));
            Scene scene = new Scene(loader.load());
            ProfileController controller = loader.getController();
            controller.setMainApp(this);
            controller.setUser(getLoggedInUser()); // Berikan data pengguna yang sedang login
            primaryStage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logout() {
        loggedInUser = null;
        showLoginView();
    }
    
    public static User getLoggedInUser() {
        return loggedInUser;
    }

    public static void main(String[] args) {
        launch(args);
    }
}