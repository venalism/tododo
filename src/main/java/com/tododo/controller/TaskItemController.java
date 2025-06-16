package com.tododo.controller;

import java.util.function.Consumer;

import com.tododo.db.TaskDAO;
import com.tododo.model.Task;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;

public class TaskItemController {

    @FXML private HBox taskItemContainer;
    @FXML private CheckBox statusCheckbox;
    @FXML private Label taskTitle;
    @FXML private Label taskDescription;
    @FXML private Label deadlineText;
    @FXML private Label statusText;
    @FXML private Label statusIcon;
    @FXML private HBox priorityBadge;
    @FXML private Label priorityText;
    @FXML private HBox statusBadge;

    private Task task;
    private Consumer<Task> onDeleteListener;
    private Consumer<Task> onEditListener;
    private Consumer<Task> onSelectedListener;

    public void setTask(Task task) {
        this.task = task;

        taskTitle.setText(task.getTitle());
        taskDescription.setText(task.getDescription());
        deadlineText.setText(
            task.getDeadline() == null || task.getDeadline().isEmpty()
                ? "Tanpa Tenggat"
                : task.getDeadline()
        );

        statusCheckbox.setSelected("selesai".equalsIgnoreCase(task.getStatus()));
        updateStatusUI();

        priorityBadge.setVisible(false);
        priorityBadge.setManaged(false);
    }

    public void setOnDeleteListener(Consumer<Task> listener) {
        this.onDeleteListener = listener;
    }

    public void setOnEditListener(Consumer<Task> listener) {
        this.onEditListener = listener;
    }

    public void setOnSelectedListener(Consumer<Task> listener) {
        this.onSelectedListener = listener;
    }

    @FXML
    private void handleDelete() {
        if (onDeleteListener != null) {
            onDeleteListener.accept(task);
        }
    }

    @FXML
    private void handleEdit() {
        if (onEditListener != null) {
            onEditListener.accept(task);
        }
    }

    @FXML
    private void handleClick(MouseEvent event) {
        if (onSelectedListener != null && task != null) {
            onSelectedListener.accept(task);
        }
    }

    @FXML
    private void toggleTaskStatus() {
        if (task != null) {
            String newStatus = statusCheckbox != null && statusCheckbox.isSelected() ? "selesai" : "tertunda";
            task.setStatus(newStatus);
            updateStatusUI();

            boolean success = TaskDAO.updateTask(task);
            if (!success) {
                if (statusCheckbox != null) {
                    statusCheckbox.setSelected(!statusCheckbox.isSelected());
                }
                updateStatusUI();
                showError("Gagal memperbarui status tugas di database.");
            }
        }
    }

    private void updateStatusUI() {
        if (task == null || task.getStatus() == null) return;

        String status = task.getStatus().toLowerCase();
        if (status.equals("selesai")) {
            statusText.setText("✅ Selesai");
            statusIcon.setText("✅");
            statusText.setStyle("-fx-text-fill: #065f46; -fx-font-weight: bold;");
            statusBadge.setStyle("-fx-background-color: #d1fae5; -fx-background-radius: 12; -fx-padding: 4 8;");
        } else {
            statusText.setText("🕐 Tertunda");
            statusIcon.setText("🕐");
            statusText.setStyle("-fx-text-fill: #92400e; -fx-font-weight: bold;");
            statusBadge.setStyle("-fx-background-color: #fef3c7; -fx-background-radius: 12; -fx-padding: 4 8;");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Kesalahan");
        alert.setHeaderText("Gagal Memperbarui");
        alert.setContentText(message);
        alert.showAndWait();
    }
} 
