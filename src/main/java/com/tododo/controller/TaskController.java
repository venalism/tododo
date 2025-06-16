// src/main/java/com/tododo/controller/TaskController.java

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

    private ObservableList<Task> masterTaskList = FXCollections.observableArrayList();
    private Task selectedTask;

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        filterAll.setToggleGroup(group);
        filterActive.setToggleGroup(group);
        filterCompleted.setToggleGroup(group);

        // Setup search listener
        searchInput.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());
        
        loadTasksFromDatabase();
    }

    @FXML
    private void handleAddTaskDialog() {
        Task newTask = showTaskDialog(null); // Pass null to indicate a new task
        if (newTask != null) {
            TaskDAO.addTask(newTask);
            loadTasksFromDatabase();
            showAlert("Berhasil", "Tugas berhasil ditambahkan.");
        }
    }

    @FXML
    private void handleEditTask() {
        if (selectedTask == null) {
            showAlert("Gagal", "Tidak ada tugas yang dipilih.");
            return;
        }
        Task editedTask = showTaskDialog(selectedTask); // Pass the selected task to be edited
        if (editedTask != null) {
            TaskDAO.updateTask(editedTask);
            loadTasksFromDatabase();
            showAlert("Berhasil", "Tugas berhasil diperbarui.");
        }
    }

    @FXML
    private void handleDeleteTask() {
        if (selectedTask == null) {
            showAlert("Gagal", "Tidak ada tugas yang dipilih.");
            return;
        }
        
        boolean confirmed = showDeleteTaskDialog(selectedTask);
        if (confirmed) {
            loadTasksFromDatabase();
            showAlert("Dihapus", "Tugas berhasil dihapus.");
        }
    }

    @FXML
    private void handleRefresh() {
        searchInput.clear();
        filterAll.setSelected(true);
        loadTasksFromDatabase();
        showAlert("Disegarkan", "Daftar tugas diperbarui.");
    }
    
    private void loadTasksFromDatabase() {
        List<Task> dbTasks = TaskDAO.getAllTasks();
        masterTaskList.setAll(dbTasks);
        applyFilters(); 
    }
    
    @FXML
    private void applyFilters() {
        ObservableList<Task> filteredList = FXCollections.observableArrayList(masterTaskList);

        // Filter by search keyword
        String keyword = searchInput.getText().toLowerCase().trim();
        if (!keyword.isEmpty()) {
            filteredList = filteredList.filtered(task ->
                task.getTitle().toLowerCase().contains(keyword) ||
                task.getDescription().toLowerCase().contains(keyword)
            );
        }

        // Filter by status (active/completed)
        if (filterActive.isSelected()) {
            filteredList = filteredList.filtered(task -> "tertunda".equalsIgnoreCase(task.getStatus()));
        } else if (filterCompleted.isSelected()) {
            filteredList = filteredList.filtered(task -> "selesai".equalsIgnoreCase(task.getStatus()));
        }

        refreshTaskListUI(filteredList);
        updateCountBadges();
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

                    controller.setTask(task);
                    controller.setOnSelectedListener(selected -> selectedTask = selected);
                    controller.setOnEditListener(this::handleEditTaskAction);
                    controller.setOnDeleteListener(this::handleDeleteTaskAction);

                    taskList.getChildren().add(taskItemNode);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    private void handleEditTaskAction(Task task) {
        selectedTask = task;
        handleEditTask();
    }

    private void handleDeleteTaskAction(Task task) {
        selectedTask = task;
        handleDeleteTask();
    }
    
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

    private boolean showDeleteTaskDialog(Task task) {
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

    private void updateCountBadges() {
        long total = masterTaskList.size();
        long active = masterTaskList.stream().filter(t -> "tertunda".equalsIgnoreCase(t.getStatus())).count();
        long completed = masterTaskList.stream().filter(t -> "selesai".equalsIgnoreCase(t.getStatus())).count();
        allCountBadge.setText(String.valueOf(total));
        activeCountBadge.setText(String.valueOf(active));
        completedCountBadge.setText(String.valueOf(completed));
    }
    
    private void showAlert(String header, String content) {
        alertMessage.setText(content);
        alertSection.setVisible(true);
        alertSection.setManaged(true);
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(event -> {
            alertSection.setVisible(false);
            alertSection.setManaged(false);
        });
        delay.play();
    }

    // Unused method from original code, can be removed if not needed.
    @FXML private void handleShowStats() {
        // ... implementation from original code ...
    }
    
    @FXML
    private void handleSearch() {
        applyFilters();
    }
}