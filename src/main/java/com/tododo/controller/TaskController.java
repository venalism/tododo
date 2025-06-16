// venalism/tododo/tododo-1e05b0edeac7f84d716f453011836aa667953f71/src/main/java/com/tododo/controller/TaskController.java
package com.tododo.controller;

import com.tododo.db.TaskDAO;
import com.tododo.model.Task;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class TaskController {
    // ... (Your FXML fields remain the same) ...
    @FXML private TextField titleField;
    @FXML private TextArea descField;
    @FXML private ToggleButton filterAll;
    @FXML private ToggleButton filterActive;
    @FXML private ToggleButton filterCompleted;
    @FXML private DatePicker deadlinePicker;
    @FXML private Label taskCountLabel;
    @FXML private Label statusInfoLabel;
    @FXML private Label alertMessage;
    @FXML private HBox alertSection;
    @FXML private VBox taskList;
    @FXML private VBox emptyState;
    @FXML private VBox addTaskForm;
    @FXML private ScrollPane taskScrollPane;
    @FXML private Label allCountBadge;
    @FXML private Label activeCountBadge;
    @FXML private Label completedCountBadge;
    @FXML private TextField searchInput;
    @FXML private VBox taskListContainer;

    private ObservableList<Task> tasks = FXCollections.observableArrayList();
    private Task selectedTask;

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        filterAll.setToggleGroup(group);
        filterActive.setToggleGroup(group);
        filterCompleted.setToggleGroup(group);

        loadTasksFromDatabase();
    }

    @FXML
    private void handleAddTaskDialog() {
        Task newTask = showTaskDialog(null); // Pass null for a new task
        if (newTask != null) {
            TaskDAO.addTask(newTask);
            loadTasksFromDatabase(); // Refresh list from DB
            showAlert("Berhasil", "Tugas berhasil ditambahkan.");
        }
    }

    @FXML
    private void handleEditTask() {
        if (selectedTask == null) {
            showAlert("Gagal", "Tidak ada tugas yang dipilih.");
            return;
        }

        Task editedTask = showTaskDialog(selectedTask); // Pass the selected task for editing
        if (editedTask != null) {
            TaskDAO.updateTask(editedTask);
            loadTasksFromDatabase(); // Refresh list from DB
            showAlert("Berhasil", "Tugas berhasil diperbarui.");
        }
    }

    @FXML
    private void handleDeleteTask() {
        if (selectedTask != null) {
            boolean confirmed = showDeleteTaskDialog(selectedTask);
            if (confirmed) {
                // The DAO is now called from the Delete Dialog, which is fine.
                // But for consistency, you could move it here as well.
                loadTasksFromDatabase();
                showAlert("Dihapus", "Tugas berhasil dihapus.");
            }
        } else {
            showAlert("Gagal", "Tidak ada tugas yang dipilih.");
        }
    }

    /**
     * Displays the dialog for adding or editing a task.
     *
     * @param task The task to edit, or null to create a new one.
     * @return The created/edited Task object, or null if canceled.
     */
    private Task showTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddTaskDialog.fxml"));
            Parent root = loader.load();

            AddTaskDialogController controller = loader.getController();
            String dialogTitle;

            if (task != null) {
                dialogTitle = "Edit Tugas";
                controller.setTaskToEdit(task);
            } else {
                dialogTitle = "Tambah Tugas";
            }

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(dialogTitle);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            return controller.getResultTask();
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka dialog tugas.");
            return null;
        }
    }
    
    // ... (The rest of your methods like filtering, searching, etc., remain the same) ...
    // ... I've included a new helper method below to reduce redundancy ...

    /**
     * Loads all tasks from the database and refreshes the UI components.
     */
    private void loadTasksFromDatabase() {
        List<Task> dbTasks = TaskDAO.getAllTasks();
        tasks.setAll(dbTasks);
        // If a filter is active, apply it. Otherwise, show all.
        if (filterActive.isSelected()) {
            filterActive();
        } else if (filterCompleted.isSelected()) {
            filterCompleted();
        } else {
            refreshTaskList(tasks);
        }
    }

    private void refreshTaskList(ObservableList<Task> data) {
        taskList.getChildren().clear();
        emptyState.setVisible(data.isEmpty());
        emptyState.setManaged(data.isEmpty());
        taskScrollPane.setVisible(!data.isEmpty());
        taskScrollPane.setManaged(!data.isEmpty());

        if (!data.isEmpty()) {
            for (Task task : data) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/task-item-template.fxml"));
                    HBox taskItem = loader.load();

                    TaskItemController controller = loader.getController();
                    controller.setTask(task);
                    
                    // Set listener for selecting a task
                    controller.setOnSelectedListener(selected -> {
                        selectedTask = selected;
                    });
                    
                    // Set listener for the edit button on a task item
                    controller.setOnEditListener(taskToEdit -> {
                        selectedTask = taskToEdit;
                        handleEditTask(); 
                    });
                    
                    // Set listener for the delete button on a task item
                    controller.setOnDeleteListener(deletedTask -> {
                        selectedTask = deletedTask;
                        handleDeleteTask();
                    });

                    taskList.getChildren().add(taskItem);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        updateCountBadges();
    }

    private void updateCountBadges() {
        long total = TaskDAO.getAllTasks().size(); // Get fresh count from DAO
        long active = tasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("tertunda")).count();
        long completed = tasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("selesai")).count();

        allCountBadge.setText(String.valueOf(total));
        activeCountBadge.setText(String.valueOf(active));
        completedCountBadge.setText(String.valueOf(completed));
    }
    
    // The rest of your code (handleRefresh, handleShowStats, filters, etc.)
    // can remain as it is.
    public boolean showDeleteTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DeleteTaskDialog.fxml"));
            Parent page = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Konfirmasi Hapus");
            dialogStage.setScene(new Scene(page));

            DeleteTaskDialogController controller = loader.getController();
            controller.setTask(task);

            dialogStage.showAndWait();
            return controller.isConfirmed();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    private void handleClearFields() {
        titleField.clear();
        descField.clear();
        deadlinePicker.setValue(null);
    }
    
    @FXML
    private void handleToggleStatus() {
        if (selectedTask != null) {
            String newStatus = selectedTask.getStatus().equalsIgnoreCase("selesai") ? "tertunda" : "selesai";
            selectedTask.setStatus(newStatus);
            boolean success = TaskDAO.updateTask(selectedTask);
            if (success) {
                loadTasksFromDatabase();
                showAlert("Berhasil", "Status tugas diperbarui.");
            } else {
                showAlert("Gagal", "Gagal memperbarui status tugas.");
            }
        } else {
            showAlert("Gagal", "Tidak ada tugas yang dipilih.");
        }
    }

    @FXML
    private void handleRefresh() {
        loadTasksFromDatabase();
        showAlert("Disegarkan", "Daftar tugas diperbarui.");
    }

    @FXML
    private void handleShowStats() {
        long total = tasks.size();
        long completed = tasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("selesai")).count();
        long active = tasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("tertunda")).count();

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistik Tugas");
        alert.setHeaderText("📊 Statistik");
        alert.setContentText(
                "Total Tugas: " + total + "\n" +
                "Selesai: " + completed + "\n" +
                "Tertunda: " + active
        );
        alert.showAndWait();
    }


    @FXML
    private void closeAlert() {
        alertSection.setVisible(false);
        alertSection.setManaged(false);
    }

    @FXML
    private void handleSearch() {
        String keyword = searchInput.getText().toLowerCase();
        ObservableList<Task> filtered = tasks.filtered(task ->
                task.getTitle().toLowerCase().contains(keyword) ||
                task.getDescription().toLowerCase().contains(keyword)
        );
        refreshTaskList(filtered);
    }

    @FXML
    private void filterAll() {
        refreshTaskList(tasks);
    }

    @FXML
    private void filterActive() {
        ObservableList<Task> filtered = tasks.filtered(task ->
                task.getStatus().equalsIgnoreCase("tertunda")
        );
        refreshTaskList(filtered);
    }

    @FXML
    private void filterCompleted() {
        ObservableList<Task> filtered = tasks.filtered(task ->
                task.getStatus().equalsIgnoreCase("selesai")
        );
        refreshTaskList(filtered);
    }
    
    private void showAlert(String header, String content) {
        alertMessage.setText(content);
        alertSection.setVisible(true);
        alertSection.setManaged(true);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> closeAlert());
        delay.play();
    }
}