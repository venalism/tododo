package com.tododo.db;

import com.tododo.model.Main;
import com.tododo.model.Task;
import com.tododo.model.User;

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
                deadline TEXT,
                user_id INTEGER,
        		FOREIGN KEY (user_id) REFERENCES users (id)
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
        User loggedInUser = Main.getLoggedInUser();
        if (loggedInUser == null) {
            System.err.println("Error: No user is logged in. Cannot fetch tasks.");
            return new ArrayList<>(); // Return an empty list if no user is logged in
        }

        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT id, title, description, status, deadline, user_id FROM tasks WHERE user_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, loggedInUser.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                // Buat objek Task dengan data dari database
                Task task = new Task(
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("deadline")
                );
                // Atur ID dan User ID secara terpisah
                task.setId(rs.getInt("id"));
                task.setUserId(rs.getInt("user_id"));
                
                tasks.add(task);
            }
        } catch (SQLException e) {
            System.err.println("Database error in getAllTasks: " + e.getMessage());
            e.printStackTrace();
        }
        return tasks;
    }

    // Tambah task baru
    public static void addTask(Task task) {
        User loggedInUser = Main.getLoggedInUser();
        if (loggedInUser == null) {
            System.err.println("Error: No user is logged in. Cannot add task.");
            return;
        }

        String sql = "INSERT INTO tasks(title, description, status, deadline, user_id) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Atur semua parameter dari objek Task
            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, task.getStatus());
            pstmt.setString(4, task.getDeadline());
            pstmt.setInt(5, loggedInUser.getId());

            int affectedRows = pstmt.executeUpdate();

            // (Opsional tapi direkomendasikan) Dapatkan ID yang dihasilkan dan atur pada objek task
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        task.setId(generatedKeys.getInt(1));
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Database error in addTask: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Hapus task berdasarkan ID
    public static boolean deleteTask(Task task) {
        User loggedInUser = Main.getLoggedInUser();
        if (loggedInUser == null) return false;

        String sql = "DELETE FROM tasks WHERE id = ? AND user_id = ?";

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, task.getId());
            stmt.setInt(2, loggedInUser.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // Perbarui data task
    public static boolean updateTask(Task task) {
        User loggedInUser = Main.getLoggedInUser();
        if (loggedInUser == null) return false;

        String sql = """
            UPDATE tasks
            SET title = ?, description = ?, status = ?, deadline = ?
            WHERE id = ? AND user_id = ?
        """;

        try (Connection conn = getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, task.getTitle());
            stmt.setString(2, task.getDescription());
            stmt.setString(3, task.getStatus());
            stmt.setString(4, task.getDeadline());
            stmt.setInt(5, task.getId());
            stmt.setInt(6, loggedInUser.getId());

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
