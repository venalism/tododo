package com.tododo.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

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
    private boolean isEditMode = false; // This flag will now be used correctly

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

        // Check if we are in edit mode
        if (isEditMode && resultTask != null) {
            // Update the existing task object
            resultTask.setTitle(title);
            resultTask.setDescription(description);
            resultTask.setStatus(status);
            resultTask.setDeadline(deadline);
        } else {
            // Create a new task object
            resultTask = new Task(title, description, status, deadline);
        }

        closeDialog();
    }

    @FXML
    private void handleCancel() {
        resultTask = null; // Ensure no task is returned on cancel
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

    /**
     * Sets the dialog to edit mode and pre-fills the fields with task data.
     * @param selectedTask The task to be edited.
     */
    public void setEditMode(Task selectedTask) {
        // FIX 1: Set the mode to true
        this.isEditMode = true; 
        this.resultTask = selectedTask; // Keep a reference to the task to be edited

        judulField.setText(selectedTask.getTitle());
        deskripsiField.setText(selectedTask.getDescription());
        statusComboBox.setValue(selectedTask.getStatus());
        
        // FIX 2: Handle empty or null deadlines safely
        if (selectedTask.getDeadline() != null && !selectedTask.getDeadline().isEmpty()) {
            try {
                deadlinePicker.setValue(LocalDate.parse(selectedTask.getDeadline()));
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse date: " + selectedTask.getDeadline());
                deadlinePicker.setValue(null);
            }
        }
    }
}