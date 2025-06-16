// venalism/tododo/tododo-1e05b0edeac7f84d716f453011836aa667953f71/src/main/java/com/tododo/controller/AddTaskDialogController.java
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
    private boolean isEditMode = false;

    /**
     * Returns the task object that was created or edited by the dialog.
     * @return The resulting Task, or null if the dialog was canceled.
     */
    public Task getResultTask() {
        return resultTask;
    }

    @FXML
    private void initialize() {
        statusComboBox.setItems(FXCollections.observableArrayList("tertunda", "selesai"));
    }

    /**
     * Configures the dialog for editing an existing task.
     * It pre-fills the form fields with the task's current data.
     * @param taskToEdit The task to be edited.
     */
    public void setTaskToEdit(Task taskToEdit) {
        this.isEditMode = true;
        this.resultTask = taskToEdit;

        judulField.setText(taskToEdit.getTitle());
        deskripsiField.setText(taskToEdit.getDescription());
        statusComboBox.setValue(taskToEdit.getStatus());

        // --- THIS IS THE FIX ---
        // Only parse the deadline if it is not null and not an empty string.
        String deadline = taskToEdit.getDeadline();
        if (deadline != null && !deadline.isEmpty()) {
            try {
                deadlinePicker.setValue(LocalDate.parse(deadline));
            } catch (DateTimeParseException e) {
                System.err.println("Could not parse deadline: " + deadline);
                deadlinePicker.setValue(null); // Set to null if parsing fails
            }
        } else {
            deadlinePicker.setValue(null);
        }
    }

    @FXML
    private void handleSave() {
        String title = judulField.getText().trim();
        String description = deskripsiField.getText().trim();
        String status = statusComboBox.getValue();
        // Ensure deadline is an empty string if the picker is empty
        String deadline = deadlinePicker.getValue() != null ? deadlinePicker.getValue().toString() : "";

        if (title.isEmpty() || status == null) {
            showError("Judul dan status tidak boleh kosong.");
            return;
        }

        if (isEditMode) {
            // If in edit mode, update the properties of the existing task object
            resultTask.setTitle(title);
            resultTask.setDescription(description);
            resultTask.setStatus(status);
            resultTask.setDeadline(deadline);
        } else {
            // If in add mode, create a new task object
            resultTask = new Task(title, description, status, deadline);
        }

        closeDialog();
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