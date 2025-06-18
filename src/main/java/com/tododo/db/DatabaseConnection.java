package com.tododo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:./tododo.db";  // Path untuk database SQLite

    // Fungsi untuk mendapatkan koneksi ke SQLite
 // venalism/tododo/tododo-1ad5fea5f4fb9fc5db25b129ae25673694c688d5/src/main/java/com/tododo/db/DatabaseConnection.java

    public static Connection getConnection() throws SQLException {
        System.out.println("[DEBUG] Meminta koneksi ke database...");
        try {
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection(URL);
            if (conn != null) {
                System.out.println("[DEBUG] Koneksi database BERHASIL dibuat.");
            }
            return conn;
        } catch (ClassNotFoundException e) {
            System.err.println("[DEBUG] FATAL: SQLite JDBC Driver tidak ditemukan.");
            e.printStackTrace();
            throw new SQLException("SQLite JDBC Driver tidak ditemukan.");
        } catch (SQLException e) {
            System.err.println("[DEBUG] FATAL: Gagal membuat koneksi ke database.");
            throw e; // Lemparkan lagi exception aslinya
        }
    }
}
