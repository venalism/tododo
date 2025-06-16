package com.tododo.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:./tododo.db";  // Path untuk database SQLite

    // Fungsi untuk mendapatkan koneksi ke SQLite
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(URL);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new SQLException("SQLite JDBC Driver tidak ditemukan.");
        }
    }
}
