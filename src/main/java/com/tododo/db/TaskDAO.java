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
    
 // venalism/tododo/tododo-1ad5fea5f4fb9fc5db25b129ae25673694c688d5/src/main/java/com/tododo/db/TaskDAO.java

 // --- Ganti metode addTask ---
 public static void addTask(Task task) {
     User loggedInUser = Main.getLoggedInUser();
     if (loggedInUser == null) {
         System.err.println("[DEBUG] Gagal addTask: Tidak ada user yang login.");
         return;
     }

     System.out.println("[DEBUG] Memanggil addTask untuk User ID: " + loggedInUser.getId());
     System.out.println("[DEBUG]    -> Judul Task: " + task.getTitle());

     String sql = "INSERT INTO tasks(title, description, status, deadline, user_id) VALUES (?, ?, ?, ?, ?)";
     
     try (Connection conn = DatabaseConnection.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
         
         pstmt.setString(1, task.getTitle());
         pstmt.setString(2, task.getDescription());
         pstmt.setString(3, task.getStatus());
         pstmt.setString(4, task.getDeadline());
         pstmt.setInt(5, loggedInUser.getId());

         int affectedRows = pstmt.executeUpdate();

         if (affectedRows > 0) {
             System.out.println("[DEBUG] addTask BERHASIL. 1 baris ditambahkan.");
         } else {
             System.err.println("[DEBUG] addTask GAGAL. Tidak ada baris yang ditambahkan.");
         }

     } catch (SQLException e) {
         System.err.println("[DEBUG] SQLException di addTask: " + e.getMessage());
         e.printStackTrace();
     }
 }


 // --- Ganti metode getAllTasks ---
 public static List<Task> getAllTasks() {
     User loggedInUser = Main.getLoggedInUser();
     if (loggedInUser == null) {
         System.err.println("[DEBUG] Gagal getAllTasks: Tidak ada user yang login.");
         return new ArrayList<>();
     }
     
     System.out.println("[DEBUG] Memanggil getAllTasks untuk User ID: " + loggedInUser.getId());
     List<Task> tasks = new ArrayList<>();
     String sql = "SELECT id, title, description, status, deadline, user_id FROM tasks WHERE user_id = ?";

     try (Connection conn = DatabaseConnection.getConnection();
          PreparedStatement pstmt = conn.prepareStatement(sql)) {
         
         pstmt.setInt(1, loggedInUser.getId());
         ResultSet rs = pstmt.executeQuery();

         while (rs.next()) {
             Task task = new Task(
                 rs.getString("title"),
                 rs.getString("description"),
                 rs.getString("status"),
                 rs.getString("deadline")
             );
             task.setId(rs.getInt("id"));
             task.setUserId(rs.getInt("user_id"));
             tasks.add(task);
         }
         
         System.out.println("[DEBUG] getAllTasks menemukan " + tasks.size() + " task.");

     } catch (SQLException e) {
         System.err.println("[DEBUG] SQLException di getAllTasks: " + e.getMessage());
         e.printStackTrace();
     }
     return tasks;
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
