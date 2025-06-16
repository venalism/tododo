package com.tododo.controller;

import java.time.LocalDate;

import com.tododo.model.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class AddTaskDialogController {

    @FXML private TextField judulField;
    @FXML private TextArea deskripsiField;
    @FXML private DatePicker deadlinePicker;
    @FXML private ComboBox<String> statusComboBox;

    private Task resultTask;
    private boolean isEditMode = false;

    public Task getResultTask() {
        return resultTask;
    }

    @FXML
    private void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList("tertunda", "selesai"));
    }

    @FXML
    private void handleSave() {
        String title = judulField.getText().trim();
        String description = deskripsiField.getText().trim();
        String status = statusComboBox.getValue();
        String deadline = deadlinePicker.getValue() != null ? deadlinePicker.getValue().toString() : "";

        if (title.isEmpty() || status == null) {
            showError("Judul dan status tidak boleh kosong.");
            return;
        }

        if (isEditMode && resultTask != null) {
            // Update task lama
            resultTask.setTitle(title);
            resultTask.setDescription(description);
            resultTask.setStatus(status);
            resultTask.setDeadline(deadline);
        } else {
            // Buat task baru
            resultTask = new Task(title, description, status, deadline);
        }

        closeDialog();
    }

    @FXML
    private void handleCancel() {
        resultTask = null;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) judulField.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Input Tidak Valid");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

 // AddTaskDialogController.java
    public void setEditMode(Task selectedTask) {
        judulField.setText(selectedTask.getTitle());
        deskripsiField.setText(selectedTask.getDescription());
        deadlinePicker.setValue(LocalDate.parse(selectedTask.getDeadline()));
        statusComboBox.setValue(selectedTask.getStatus());

        // Ketika tombol simpan ditekan, update task, bukan buat baru
        this.resultTask = selectedTask;
    }

}
