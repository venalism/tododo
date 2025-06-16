package com.tododo.model;

import javafx.beans.property.*;

public class Task {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final StringProperty status = new SimpleStringProperty("tertunda");
    private final StringProperty deadline = new SimpleStringProperty();
    private final IntegerProperty userId = new SimpleIntegerProperty();

    public Task() {
        // Default constructor
    }

    public Task(String title, String description, String status, String deadline) {
        setTitle(title);
        setDescription(description);
        setStatus(status);
        setDeadline(deadline);
    }

    public Task(int id, String title, String description, String status, String deadline) {
        this(title, description, status, deadline);
        setId(id);
    }

    // Getters
    public int getId() {
        return id.get();
    }

    public String getTitle() {
        return title.get();
    }

    public String getDescription() {
        return description.get();
    }

    public String getStatus() {
        return status.get();
    }

    public String getDeadline() {
        return deadline.get();
    }

    // Setters
    public void setId(int id) {
        this.id.set(id);
    }

    public void setTitle(String title) {
        this.title.set(title != null ? title : "");
    }

    public void setDescription(String description) {
        this.description.set(description != null ? description : "");
    }

    public void setStatus(String newStatus) {
        this.status.set(newStatus != null ? newStatus : "tertunda");
    }

    public void setDeadline(String deadline) {
        this.deadline.set(deadline != null ? deadline : "");
    }

    // Property accessors
    public IntegerProperty idProperty() {
        return id;
    }

    public StringProperty titleProperty() {
        return title;
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public StringProperty statusProperty() {
        return status;
    }

    public StringProperty deadlineProperty() {
        return deadline;
    }
    
    public int getUserId() {
        return userId.get();
    }
    public void setUserId(int userId) {
        this.userId.set(userId);
    }
    public IntegerProperty userIdProperty() {
        return userId;
    }

    @Override
    public String toString() {
        return "Task{" +
               "id=" + getId() +
               ", title='" + getTitle() + '\'' +
               ", description='" + getDescription() + '\'' +
               ", status='" + getStatus() + '\'' +
               ", deadline='" + getDeadline() + '\'' +
               '}';
    }
}
