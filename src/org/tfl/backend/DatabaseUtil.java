package org.tfl.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 * Utility class for database operations
 */
public class DatabaseUtil {

    private static final Logger log = Logger.getLogger(DatabaseUtil.class.getName());
    private static DataSource dataSource;

    // Initialize the DataSource
    static {
        try {
            Context initContext = new InitialContext();
            Context envContext = (Context) initContext.lookup("java:comp/env");
            dataSource = (DataSource) envContext.lookup("jdbc/secureapp");
        } catch (NamingException e) {
            log.log(Level.SEVERE, "Could not initialize DataSource: " + e.getMessage(), e);
        }
    }

    /**
     * Get a database connection from the connection pool
     * @return Connection object
     * @throws SQLException if a database error occurs
     */
    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Close all database resources safely
     * @param conn Database connection
     * @param stmt PreparedStatement
     * @param rs ResultSet
     */
    public static void closeResources(Connection conn, PreparedStatement stmt, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Error closing ResultSet: " + e.getMessage(), e);
        }

        try {
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Error closing PreparedStatement: " + e.getMessage(), e);
        }

        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            log.log(Level.WARNING, "Error closing Connection: " + e.getMessage(), e);
        }
    }
}