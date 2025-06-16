// src/main/java/com/tododo/controller/AddTaskDialogController.java
package com.tododo.controller;

import com.tododo.model.Task;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class AddTaskDialogController {

    @FXML private TextField judulField;
    @FXML private TextArea deskripsiField;
    @FXML private DatePicker deadlinePicker;
    @FXML private ComboBox<String> statusComboBox;

    private Task resultTask;
    private boolean isEditMode = false; // Flag to determine if we are adding or editing

    /**
     * Initializes the controller, setting up the status choices.
     */
    @FXML
    private void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList("tertunda", "selesai"));
    }

    /**
     * Configures the dialog for editing an existing task.
     * This method is called by TaskController when the user wants to edit a task.
     * It pre-fills the form fields with the task's current data.
     * @param taskToEdit The task to be edited.
     */
    public void setTaskToEdit(Task taskToEdit) {
        this.isEditMode = true; // Set the flag to edit mode
        this.resultTask = taskToEdit; // Store the task object that will be modified

        // Populate the UI fields with the task's data
        judulField.setText(taskToEdit.getTitle());
        deskripsiField.setText(taskToEdit.getDescription());
        statusComboBox.setValue(taskToEdit.getStatus());

        String deadline = taskToEdit.getDeadline();
        if (deadline != null && !deadline.isEmpty()) {
            try {
                deadlinePicker.setValue(LocalDate.parse(deadline));
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse deadline: " + deadline);
                deadlinePicker.setValue(null);
            }
        } else {
            deadlinePicker.setValue(null);
        }
    }
    
    /**
     * Handles the save action. It checks if the dialog is in edit mode
     * to either update the existing task or create a new one.
     */
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

        if (isEditMode) {
            // In edit mode, update the properties of the existing task object.
            // Because `resultTask` is a reference to the original object, these changes
            // will be reflected in the TaskController.
            resultTask.setTitle(title);
            resultTask.setDescription(description);
            resultTask.setStatus(status);
            resultTask.setDeadline(deadline);
        } else {
            // In add mode, create a brand new task object
            resultTask = new Task(title, description, status, deadline);
        }

        closeDialog();
    }

    /**
     * Returns the task object that was created or edited.
     * @return The resulting Task, or null if the dialog was canceled.
     */
    public Task getResultTask() {
        return resultTask;
    }

    @FXML
    private void handleCancel() {
        resultTask = null; // Ensure no result is returned on cancellation
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
}