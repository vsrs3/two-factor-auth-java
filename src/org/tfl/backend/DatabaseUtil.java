package org.tfl.backend;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseUtil {
    private static final Logger log = Logger.getLogger(DatabaseUtil.class.getName());

    // Database connection parameters
    private static final String DB_URL = "jdbc:mysql://localhost:6688/secureapp";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "0819139431";

    static {
        try {
            // Load the MySQL JDBC driver
            Class.forName("com.mysql.cj.jdbc.Driver");
            log.info("MySQL JDBC driver loaded successfully");
        } catch (ClassNotFoundException e) {
            log.log(Level.SEVERE, "Could not load MySQL JDBC driver", e);
        }
    }

    /**
     * Get a database connection
     * @return Connection object
     * @throws SQLException if a database error occurs
     */
    public static Connection getConnection() throws SQLException {
        try {
            Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            if (conn != null) {
                log.info("Database connection established successfully");
            }
            return conn;
        } catch (SQLException e) {
            log.log(Level.SEVERE, "Failed to establish database connection", e);
            throw e;
        }
    }

    /**
     * Close all database resources safely
     * @param conn Database connection
     * @param stmt PreparedStatement
     * @param rs ResultSet
     */
    public static void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        // [Existing code to close resources]
    }
}