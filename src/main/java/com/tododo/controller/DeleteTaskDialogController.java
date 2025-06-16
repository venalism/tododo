package com.tododo.controller;

import com.tododo.model.Task;
import com.tododo.db.TaskDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class DeleteTaskDialogController {

    @FXML private Label messageLabel;
    @FXML private Label taskTitleLabel;

    private boolean confirmed = false;
    private Task taskToDelete;

    public void setTask(Task task) {
        this.taskToDelete = task;
        messageLabel.setText("Apakah Anda yakin ingin menghapus tugas ini?");
        taskTitleLabel.setText(task.getTitle());
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void handleDelete() {
        if (taskToDelete != null) {
            boolean success = TaskDAO.deleteTask(taskToDelete);
            if (success) {
                confirmed = true;
            } else {
                showError("Gagal menghapus tugas dari database.");
            }
        }
        closeWindow();
    }

    @FXML
    private void handleCancel() {
        confirmed = false;
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) messageLabel.getScene().getWindow()).close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Kesalahan");
        alert.setHeaderText("Terjadi kesalahan");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
