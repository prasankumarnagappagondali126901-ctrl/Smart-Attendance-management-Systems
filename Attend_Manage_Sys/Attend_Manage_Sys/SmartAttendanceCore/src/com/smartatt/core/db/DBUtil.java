package com.smartatt.core.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBUtil {
    // Use the same URL you tested earlier in Main
    private static final String URL = "jdbc:mysql://localhost:3307/smart_attendance?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = ""; // change when you make a dedicated user

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            // fatal for JDBC usage — rethrow as runtime so it fails fast
            throw new RuntimeException("MySQL JDBC driver not found on classpath", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
