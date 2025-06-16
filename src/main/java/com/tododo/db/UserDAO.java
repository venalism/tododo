package com.tododo.db;

import com.tododo.model.User;
import com.tododo.utils.PasswordUtils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class UserDAO {

    static {
        createTableIfNotExists();
    }

    private static void createTableIfNotExists() {
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL
            );
        """;
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean registerUser(User user) {
        String sql = "INSERT INTO users(username, email, password) VALUES(?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getEmail());
            pstmt.setString(3, PasswordUtils.hashPassword(user.getPassword()));
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace(); // Consider more specific error handling
            return false;
        }
    }

    public static User loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                String hashedPassword = rs.getString("password");
                if (PasswordUtils.verifyPassword(password, hashedPassword)) {
                    User user = new User(rs.getString("username"), rs.getString("email"), null); // Don't pass password back
                    user.setId(rs.getInt("id"));
                    return user;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}