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
    private TaskDAO taskDAO;
    
    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        filterAll.setToggleGroup(group);
        filterActive.setToggleGroup(group);
        filterCompleted.setToggleGroup(group);

        // Setup search listener
        searchInput.textProperty().addListener((obs, oldVal, newVal) -> handleSearch());

        filterAll.setSelected(true);
        loadTasksFromDatabase();
    }

    @FXML
    private void handleAddTaskDialog() {
        Task newTask = showTaskDialog(null);
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

        Task editedTask = showTaskDialog(selectedTask);
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
            TaskDAO.deleteTask(selectedTask);
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

    @FXML
    private void closeAlert() {
        alertSection.setVisible(false);
        alertSection.setManaged(false);
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
    
    @FXML
    private void filterAll() {
        List<Task> tasks = taskDAO.getAllTasks();
        showTaskList(tasks);
        updateFilterButtonStyles();
    }

    @FXML
    private void filterActive() {
        List<Task> tasks = taskDAO.getActiveTasks(); // misal: status = false
        showTaskList(tasks);
        updateFilterButtonStyles();
    }

    @FXML
    private void filterCompleted() {
        List<Task> tasks = taskDAO.getCompletedTasks(); // misal: status = true
        showTaskList(tasks);
        updateFilterButtonStyles();
    }

    @FXML
    private void handleToggleStatus() {
        if (selectedTask != null) {
            boolean newStatus = !selectedTask.isCompleted();
            selectedTask.setCompleted(newStatus);
            taskDAO.updateTaskStatus(selectedTask.getId(), newStatus);
            showAlert("Status tugas diperbarui.");
            refreshFilteredList();
        } else {
            showAlert("Pilih tugas terlebih dahulu.");
        }
    }

    @FXML
    private void handleShowStats() {
        int total = taskDAO.countAll();
        int completed = taskDAO.countCompleted();
        int active = total - completed;

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Statistik Tugas");
        alert.setHeaderText("📊 Statistik Saat Ini");
        alert.setContentText(
            "Total: " + total + "\n" +
            "Aktif: " + active + "\n" +
            "Selesai: " + completed
        );
        alert.showAndWait();
    }
    
    private void updateFilterButtonStyles() {
        filterAll.setStyle("");
        filterActive.setStyle("");
        filterCompleted.setStyle("");

        switch (currentFilter) {
            case "ALL" -> filterAll.setStyle("-fx-background-color: lightblue;");
            case "ACTIVE" -> filterActive.setStyle("-fx-background-color: lightblue;");
            case "COMPLETED" -> filterCompleted.setStyle("-fx-background-color: lightblue;");
        }
    }

    
    private void showTaskList(List<Task> tasks) {
        taskList.getChildren().clear();
        for (Task task : tasks) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/task-item-template.fxml"));
                HBox node = loader.load();
                TaskItemController controller = loader.getController();
                controller.setTask(task);
                controller.setOnSelectedListener(selected -> selectedTask = selected);
                taskList.getChildren().add(node);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private String currentFilter = "ALL";

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
            String dialogTitle = (task != null) ? "Edit Tugas" : "Tambah Tugas";
            if (task != null) controller.setTaskToEdit(task);

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

    @FXML private void handleSearch() {
        applyFilters();
    }
}
