package org.tfl.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for user information operations
 */
public class UserDAO
{
    private static final Logger log = Logger.getLogger(UserDAO.class.getName());

    /**
     * Retrieves the full name for a userid
     *
     * @param userid User ID
     * @param remoteip Client IP address
     * @return full name if successful, null if there is an error
     */
    public static String getUserName(String userid, String remoteip)
    {
        if (userid == null || remoteip == null) {
            log.warning("Invalid parameters for getUserName - " + remoteip);
            return null;
        }

        String fullName = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();

            // Query user information
            String sql = "SELECT firstname, lastname FROM users WHERE userid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userid);

            rs = stmt.executeQuery();

            if (rs.next()) {
                String firstName = rs.getString("firstname");
                String lastName = rs.getString("lastname");

                // Concatenate first name and last name
                fullName = firstName + " " + lastName;
            } else {
                log.warning("User not found: " + userid + " - " + remoteip);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error retrieving user name: " + e.getMessage(), e);
        } finally {
            // Close resources
            DatabaseUtil.closeResources(conn, stmt, rs);
        }

        return fullName;
    }

    /**
     * Gets the security level of a user
     *
     * @param userid User ID
     * @param remoteip Client IP address
     * @return Security level (1-4) or 0 if user not found
     */
    public static int getUserSecurityLevel(String userid, String remoteip)
    {
        if (userid == null || remoteip == null) {
            log.warning("Invalid parameters for getUserSecurityLevel - " + remoteip);
            return 0;
        }

        int securityLevel = 0;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();

            // Query user security level
            String sql = "SELECT label FROM users WHERE userid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userid);

            rs = stmt.executeQuery();

            if (rs.next()) {
                securityLevel = rs.getInt("label");
            } else {
                log.warning("User not found for security level: " + userid + " - " + remoteip);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error retrieving security level: " + e.getMessage(), e);
        } finally {
            // Close resources
            DatabaseUtil.closeResources(conn, stmt, rs);
        }

        return securityLevel;
    }

    /**
     * Set the security level for a user (admin only)
     *
     * @param adminId Admin user ID
     * @param targetUserId Target user ID to update
     * @param securityLevel New security level
     * @param remoteip Client IP address
     * @return true if update successful, false otherwise
     */
    public static boolean setUserSecurityLevel(String adminId, String targetUserId,
                                               int securityLevel, String remoteip)
    {
        if (adminId == null || targetUserId == null || remoteip == null ||
                securityLevel < AppConstants.SECURITY_UNCLASSIFIED ||
                securityLevel > AppConstants.SECURITY_TOP_SECRET) {
            log.warning("Invalid parameters for setUserSecurityLevel - " + remoteip);
            return false;
        }

        // Check if user is admin
        if (!"admin".equals(adminId)) {
            log.warning("Non-admin user attempted to set security level: " +
                    adminId + " - " + remoteip);
            return false;
        }

        boolean success = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();

            // Update user security level
            String sql = "UPDATE users SET label = ? WHERE userid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, securityLevel);
            stmt.setString(2, targetUserId);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                success = true;
                log.info("Security level updated: " + targetUserId +
                        " to level " + securityLevel + " by " + adminId + " - " + remoteip);
            } else {
                log.warning("Failed to update security level: " + targetUserId +
                        " - Target user not found - " + remoteip);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error updating security level: " + e.getMessage(), e);
        } finally {
            // Close resources
            DatabaseUtil.closeResources(conn, stmt, null);
        }

        return success;
    }
}