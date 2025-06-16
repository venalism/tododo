package com.tododo.db;

import com.tododo.model.Task;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TaskDAO {

    // Inisialisasi otomatis: buat tabel jika belum ada
    static {
        createTableIfNotExists();
    }

    // Gunakan koneksi dari DatabaseConnection secara konsisten
    private static Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    private static void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS tasks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                title TEXT NOT NULL,
                description TEXT NOT NULL,
                status TEXT NOT NULL,
                deadline TEXT
            );
        """;

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Ambil semua task dari database
    public static List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String status = rs.getString("status");
                String deadline = rs.getString("deadline");

                Task task = new Task(title, description, status, deadline);
                task.setId(id);
                tasks.add(task);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return tasks;
    }

    // Tambah task baru
    public static void addTask(Task task) {
        String sql = "INSERT INTO tasks(title, description, status, deadline) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getStatus());
            pstmt.setString(4, task.getDeadline());
            pstmt.executeUpdate();

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    task.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    // Hapus task berdasarkan ID
    public static boolean deleteTask(Task task) {
        String sql = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, task.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Perbarui data task
    public static boolean updateTask(Task task) {
        String sql = "UPDATE tasks SET title = ?, description = ?, status = ?, deadline = ? WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus());
            stmt.setString(4, task.getDeadline());
            stmt.setInt(5, task.getId());

            return stmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Ambil satu task berdasarkan ID
    public static Task getTaskById(int id) {
        String sql = "SELECT id, title, description, status, deadline FROM tasks WHERE id = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Task task = new Task(
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("deadline")
                );
                task.setId(rs.getInt("id"));
                return task;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
