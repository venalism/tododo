// src/main/java/com/tododo/controller/TaskController.java
package com.tododo.controller;

import com.tododo.db.TaskDAO;
import com.tododo.model.Main;
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
import java.util.Optional;

public class TaskController {

    // FXML UI Elements
    @FXML private TextField searchInput;
    @FXML private ToggleButton filterAll;
    @FXML private ToggleButton filterActive;
    @FXML private ToggleButton filterCompleted;
    @FXML private Label allCountBadge;
    @FXML private Label activeCountBadge;
    @FXML private Label completedCountBadge;
    @FXML private VBox taskList;
    @FXML private VBox emptyState;
    @FXML private ScrollPane taskScrollPane;
    @FXML private HBox alertSection;
    @FXML private Label alertMessage;

    // Data handling
    private final ObservableList<Task> masterTaskList = FXCollections.observableArrayList();
    private Task selectedTask;
    private Main mainApp;
    
    public void setMainApp(Main mainApp) {
        this.mainApp = mainApp;
    }

    @FXML
    public void initialize() {
        // Configure the filter toggle buttons so only one can be selected at a time
        ToggleGroup filterGroup = new ToggleGroup();
        filterAll.setToggleGroup(filterGroup);
        filterActive.setToggleGroup(filterGroup);
        filterCompleted.setToggleGroup(filterGroup);
        filterAll.setSelected(true); // Default selection

        // Add a listener to the search input to filter as the user types
        searchInput.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Load initial data from the database
        loadTasksFromDatabase();
    }

    // --- Data Loading and UI Refresh ---

    private void loadTasksFromDatabase() {
        List<Task> dbTasks = TaskDAO.getAllTasks();
        masterTaskList.setAll(dbTasks);
        applyFilters();
    }

    private void refreshTaskListUI(ObservableList<Task> displayList) {
        taskList.getChildren().clear();

        boolean isEmpty = displayList.isEmpty();
        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);
        taskScrollPane.setVisible(!isEmpty);
        taskScrollPane.setManaged(!isEmpty);

        if (!isEmpty) {
            for (Task task : displayList) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/task-item-template.fxml"));
                    HBox taskItemNode = loader.load();
                    TaskItemController controller = loader.getController();

                    // Set the task data and listeners for each item
                    controller.setTask(task);
                    controller.setOnSelectedListener(clickedTask -> selectedTask = clickedTask); // Update selectedTask on click
                    controller.setOnEditListener(this::handleEditTaskAction);
                    controller.setOnDeleteListener(this::handleDeleteTaskAction);

                    taskList.getChildren().add(taskItemNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // --- Filter Methods (Called by FXML) ---

    @FXML
    private void filterAll() {
        applyFilters();
    }

    @FXML
    private void filterActive() {
        applyFilters();
    }

    @FXML
    private void filterCompleted() {
        applyFilters();
    }
    
    @FXML
    private void handleSearch() {
        applyFilters();
    }
    
    @FXML
    private void handleShowProfile() {
        if (mainApp != null) {
            mainApp.showProfileView();
        }
    }
    
    private void applyFilters() {
        ObservableList<Task> filteredList = FXCollections.observableArrayList(masterTaskList);

        String keyword = searchInput.getText().toLowerCase().trim();
        if (!keyword.isEmpty()) {
            filteredList = filteredList.filtered(task ->
                task.getTitle().toLowerCase().contains(keyword) ||
                task.getDescription().toLowerCase().contains(keyword)
            );
        }

        if (filterActive.isSelected()) {
            filteredList = filteredList.filtered(task -> "tertunda".equalsIgnoreCase(task.getStatus()));
        } else if (filterCompleted.isSelected()) {
            filteredList = filteredList.filtered(task -> "selesai".equalsIgnoreCase(task.getStatus()));
        }

        refreshTaskListUI(filteredList);
        updateCountBadges();
    }

    // --- Main Action Handlers (Called by FXML) ---

    @FXML
    private void handleAddTaskDialog() {
        showTaskDialog(null).ifPresent(newTask -> {
            TaskDAO.addTask(newTask);
            loadTasksFromDatabase();
            showAlert("Berhasil", "Tugas berhasil ditambahkan.");
        });
    }

    @FXML
    private void handleEditTask() {
        if (selectedTask == null) {
            showAlert("Gagal", "Tidak ada tugas yang dipilih. Klik sebuah tugas terlebih dahulu.");
            return;
        }
        handleEditTaskAction(selectedTask);
    }
    
    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null) {
            showAlert("Gagal", "Tidak ada tugas yang dipilih. Klik sebuah tugas terlebih dahulu.");
            return;
        }
        handleDeleteTaskAction(selectedTask);
    }
    
    @FXML
    private void handleToggleStatus() {
        if (selectedTask == null) {
            showAlert("Gagal", "Tidak ada tugas yang dipilih. Klik sebuah tugas terlebih dahulu.");
            return;
        }
        // Toggle status
        String newStatus = "selesai".equalsIgnoreCase(selectedTask.getStatus()) ? "tertunda" : "selesai";
        selectedTask.setStatus(newStatus);
        
        // Update database and refresh UI
        TaskDAO.updateTask(selectedTask);
        applyFilters();
        showAlert("Berhasil", "Status tugas telah diperbarui.");
    }
    
    @FXML
    private void handleShowStats() {
        long total = masterTaskList.size();
        long active = masterTaskList.stream().filter(t -> "tertunda".equalsIgnoreCase(t.getStatus())).count();
        long completed = total - active;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistik Tugas");
        alert.setHeaderText("📊 Statistik Saat Ini");
        alert.setContentText(
            "Total Tugas: " + total + "\n" +
            "Aktif: " + active + "\n" +
            "Selesai: " + completed
        );
        alert.showAndWait();
    }

    @FXML
    private void handleRefresh() {
        searchInput.clear();
        filterAll.setSelected(true);
        loadTasksFromDatabase();
        showAlert("Disegarkan", "Daftar tugas diperbarui.");
    }

    // --- Helper and Dialog Methods ---

    private void handleEditTaskAction(Task taskToEdit) {
        showTaskDialog(taskToEdit).ifPresent(editedTask -> {
            TaskDAO.updateTask(editedTask);
            applyFilters();
            showAlert("Berhasil", "Tugas berhasil diperbarui.");
        });
    }

    private void handleDeleteTaskAction(Task taskToDelete) {
        if (showDeleteTaskDialog(taskToDelete)) {
            TaskDAO.deleteTask(taskToDelete);
            masterTaskList.remove(taskToDelete);
            applyFilters();
            showAlert("Dihapus", "Tugas berhasil dihapus.");
        }
    }

    private Optional<Task> showTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddTaskDialog.fxml"));
            Parent root = loader.load();
            AddTaskDialogController controller = loader.getController();
            String dialogTitle = (task != null) ? "Edit Tugas" : "Tambah Tugas";
            if (task != null) {
                controller.setTaskToEdit(task);
            }
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle(dialogTitle);
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();
            return Optional.ofNullable(controller.getResultTask());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Gagal membuka dialog tugas.");
            return Optional.empty();
        }
    }

    private boolean showDeleteTaskDialog(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/DeleteTaskDialog.fxml"));
            Parent page = loader.load();
            DeleteTaskDialogController controller = loader.getController();
            controller.setTask(task);
            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Konfirmasi Hapus");
            dialogStage.setScene(new Scene(page));
            dialogStage.showAndWait();
            return controller.isConfirmed();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    private void updateCountBadges() {
        long total = masterTaskList.size();
        long active = masterTaskList.stream().filter(t -> "tertunda".equalsIgnoreCase(t.getStatus())).count();
        long completed = total - active;
        allCountBadge.setText(String.valueOf(total));
        activeCountBadge.setText(String.valueOf(active));
        completedCountBadge.setText(String.valueOf(completed));
    }

    private void showAlert(String header, String content) {
        alertMessage.setText(content);
        alertSection.setVisible(true);
        alertSection.setManaged(true);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> closeAlert());
        delay.play();
    }

    @FXML
    private void closeAlert() {
        alertSection.setVisible(false);
        alertSection.setManaged(false);
    }
}