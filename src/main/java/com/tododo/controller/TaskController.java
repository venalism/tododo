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
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;

public class TaskController {
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

    public ObservableList<Task> getTasks() {
        return tasks;
    }

    @FXML
    public void initialize() {
        ToggleGroup group = new ToggleGroup();
        filterAll.setToggleGroup(group);
        filterActive.setToggleGroup(group);
        filterCompleted.setToggleGroup(group);

        List<Task> dbTasks = TaskDAO.getAllTasks();
        tasks.setAll(dbTasks);
        refreshTaskList(tasks);
    }

    @FXML
    private void handleAddTaskDialog() {
        Task newTask = showAddTaskDialog();
        if (newTask != null) {
            TaskDAO.addTask(newTask);
            tasks.setAll(TaskDAO.getAllTasks());
            refreshTaskList(tasks);
            showAlert("Berhasil", "Tugas berhasil ditambahkan.");
        }
    }

    public Task showAddTaskDialog() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddTaskDialog.fxml"));
            Parent root = loader.load();

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.setTitle("Tambah Tugas");
            dialogStage.setScene(new Scene(root));
            dialogStage.showAndWait();

            AddTaskDialogController controller = loader.getController();
            return controller.getResultTask();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

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
    private void handleDeleteTask() {
        if (selectedTask != null) {
            boolean confirmed = showDeleteTaskDialog(selectedTask);
            if (confirmed) {
                TaskDAO.deleteTask(selectedTask);
                tasks.setAll(TaskDAO.getAllTasks());
                refreshTaskList(tasks);
                showAlert("Dihapus", "Tugas berhasil dihapus.");
            }
        } else {
            showAlert("Gagal", "Tidak ada tugas yang dipilih.");
        }
    }

 // In TaskController.java
    @FXML
    private void handleEditTask() {
        if (selectedTask != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/AddTaskDialog.fxml"));
                Parent root = loader.load();

                AddTaskDialogController controller = loader.getController();
                // This sets the dialog to edit mode and passes the selected task
                controller.setEditMode(selectedTask); 

                Stage dialogStage = new Stage();
                dialogStage.initModality(Modality.APPLICATION_MODAL);
                dialogStage.setTitle("Edit Tugas");
                dialogStage.setScene(new Scene(root));
                dialogStage.showAndWait();

                // The controller's getResultTask() will now return the *updated* task
                Task editedTask = controller.getResultTask();

                // Check if the user saved the changes (result is not null)
                if (editedTask != null) {
                    // The task object is already updated, so we just need to save it to the DB
                    boolean success = TaskDAO.updateTask(editedTask);
                    if (success) {
                        // Refresh the entire list to show the change
                        tasks.setAll(TaskDAO.getAllTasks());
                        refreshTaskList(tasks);
                        showAlert("Berhasil", "Tugas berhasil diperbarui.");
                    } else {
                        showAlert("Gagal", "Gagal menyimpan perubahan ke database.");
                    }
                }
                // If the user cancelled, editedTask will be null, and we do nothing.

            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Gagal membuka dialog edit.");
            }
        } else {
            showAlert("Gagal", "Tidak ada tugas yang dipilih.");
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
                refreshTaskList(FXCollections.observableArrayList(TaskDAO.getAllTasks()));
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
        tasks.setAll(TaskDAO.getAllTasks());
        refreshTaskList(tasks);
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

    private void refreshTaskList(ObservableList<Task> data) {
        taskList.getChildren().clear();

        boolean isEmpty = data.isEmpty();

        emptyState.setVisible(isEmpty);
        emptyState.setManaged(isEmpty);

        taskScrollPane.setVisible(!isEmpty);
        taskScrollPane.setManaged(!isEmpty);

        if (!isEmpty) {
            for (Task task : data) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/task-item-template.fxml"));
                    HBox taskItem = loader.load();

                    TaskItemController controller = loader.getController();
                    controller.setTask(task);

                    controller.setOnDeleteListener(deletedTask -> {
                        TaskDAO.deleteTask(deletedTask);
                        tasks.setAll(TaskDAO.getAllTasks());
                        refreshTaskList(tasks);
                    });

                    controller.setOnSelectedListener(selected -> {
                        selectedTask = selected;
                    });
                    
                    controller.setOnEditListener(taskToEdit -> {
                        selectedTask = taskToEdit;
                        handleEditTask(); 
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
        long total = tasks.size();
        long active = tasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("tertunda")).count();
        long completed = tasks.stream().filter(t -> t.getStatus().equalsIgnoreCase("selesai")).count();

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
}
