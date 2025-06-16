package com.tododo.controller;

import com.tododo.model.Task;
import com.tododo.db.TaskDAO;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class EditTaskDialogController {

    @FXML private TextField judulField;
    @FXML private TextArea deskripsiField;
    @FXML private DatePicker deadlinePicker;
    @FXML private ComboBox<String> statusComboBox;

    private Task task;
    private boolean saved = false;

    public void setTask(Task task) {
        this.task = task;
        judulField.setText(task.getTitle());
        deskripsiField.setText(task.getDescription());
        statusComboBox.setValue(task.getStatus());
        if (!task.getDeadline().isEmpty()) {
            deadlinePicker.setValue(java.time.LocalDate.parse(task.getDeadline()));
        }
    }

    public boolean isSaved() {
        return saved;
    }

    @FXML
    public void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList("tertunda", "selesai"));
    }

    @FXML
    private void handleSave() {
        // Perbarui objek Task
        task.titleProperty().set(judulField.getText());
        task.descriptionProperty().set(deskripsiField.getText());
        task.statusProperty().set(statusComboBox.getValue());
        task.deadlineProperty().set(deadlinePicker.getValue() != null ? deadlinePicker.getValue().toString() : "");

        // Simpan ke database
        boolean success = TaskDAO.updateTask(task);
        if (success) {
            saved = true;
        } else {
            showError("Gagal menyimpan perubahan tugas ke database.");
        }

        closeWindow();
    }

    @FXML
    private void handleCancel() {
        closeWindow();
    }

    private void closeWindow() {
        ((Stage) judulField.getScene().getWindow()).close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Kesalahan");
        alert.setHeaderText("Gagal Menyimpan");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
