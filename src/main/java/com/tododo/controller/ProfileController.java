package com.tododo.controller;

import com.tododo.db.UserDAO;
import com.tododo.model.Main;
import com.tododo.model.User;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;

import java.util.Optional;

public class ProfileController {

    @FXML
    private Label usernameLabel;
    @FXML
    private Label emailLabel;

    private Main mainApp;
    private User currentUser;

    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    public void setUser(User user) {
        this.currentUser = user;
        updateLabels();
    }

    private void updateLabels() {
        if (currentUser != null) {
            usernameLabel.setText(currentUser.getUsername());
            emailLabel.setText(currentUser.getEmail());
        }
    }

    @FXML
    private void handleLogout() {
        mainApp.logout();
    }

    @FXML
    private void handleBack() {
        mainApp.showTaskView(currentUser);
    }

    @FXML
    private void handleEditAccount() {
        // Buat dialog kustom atau gunakan beberapa TextInputDialog
        TextInputDialog usernameDialog = new TextInputDialog(currentUser.getUsername());
        usernameDialog.setTitle("Edit Akun");
        usernameDialog.setHeaderText("Masukkan username baru.");
        Optional<String> usernameResult = usernameDialog.showAndWait();

        if (usernameResult.isPresent()) {
            TextInputDialog emailDialog = new TextInputDialog(currentUser.getEmail());
            emailDialog.setTitle("Edit Akun");
            emailDialog.setHeaderText("Masukkan email baru.");
            Optional<String> emailResult = emailDialog.showAndWait();

            if (emailResult.isPresent()) {
                currentUser.setUsername(usernameResult.get());
                currentUser.setEmail(emailResult.get());

                // Untuk simplicity, kita tidak akan mengedit password di sini
                // Anda bisa menambahkan dialog lain untuk password jika diperlukan

                if (UserDAO.updateUser(currentUser)) {
                    showAlert(Alert.AlertType.INFORMATION, "Sukses", "Akun berhasil diperbarui.");
                    updateLabels();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Gagal", "Tidak dapat memperbarui akun.");
                }
            }
        }
    }

    @FXML
    private void handleDeleteAccount() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Hapus Akun");
        confirmation.setHeaderText("Apakah Anda yakin ingin menghapus akun Anda?");
        confirmation.setContentText("Tindakan ini tidak dapat diurungkan.");

        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (UserDAO.deleteUser(currentUser.getId())) {
                showAlert(Alert.AlertType.INFORMATION, "Sukses", "Akun berhasil dihapus.");
                mainApp.logout();
            } else {
                showAlert(Alert.AlertType.ERROR, "Gagal", "Tidak dapat menghapus akun.");
            }
        }
    }
    
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}