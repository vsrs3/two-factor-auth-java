package org.tfl.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;

import org.tfl.crypto.CryptoUtil;

/**
 * Data Access Object for OTP operations
 */
public class OTPDAO
{
    private static final Logger log = Logger.getLogger(OTPDAO.class.getName());

    /**
     * Retrieves the OTP secret hexadecimal string from the userid
     *
     * @param userid User ID
     * @param remoteip Client IP address
     * @return hexadecimal secret string 
     * @throws ServletException if a servlet error occurs
     */
    public static String getOTPSecret(String userid, String remoteip) throws ServletException
    {
        if (userid == null || remoteip == null) {
            throw new ServletException("Invalid parameters: userid or remoteip is null");
        }

        String otpSecret = null;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();

            // Query OTP secret
            String sql = "SELECT otpsecret FROM users WHERE userid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userid);

            rs = stmt.executeQuery();

            if (rs.next()) {
                otpSecret = rs.getString("otpsecret");
            } else {
                log.warning("OTP secret not found for user: " + userid + " - " + remoteip);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error retrieving OTP secret: " + e.getMessage(), e);
            throw new ServletException("Database error retrieving OTP secret", e);
        } finally {
            // Close resources
            DatabaseUtil.closeResources(conn, stmt, rs);
        }

        return otpSecret;
    }

    /**
     * Retrieves the OTP secret in Base32 encoding format for Google Authenticator
     *
     * @param userid User ID
     * @param remoteip Client IP address
     * @return Base32 encoded secret string
     * @throws ServletException if a servlet error occurs
     */
    public static String getBase32OTPSecret(String userid, String remoteip) throws ServletException
    {
        String hexaString = getOTPSecret(userid, remoteip);

        if (hexaString == null) {
            return null;
        }

        // Convert hex to byte array and then to Base32
        byte[] hexaCode = CryptoUtil.hexStringToByteArray(hexaString);

        if (hexaCode == null) {
            log.warning("Failed to convert OTP secret to byte array: " + userid + " - " + remoteip);
            return null;
        }

        return CryptoUtil.base32Encode(hexaCode);
    }

    /**
     * Check if a user account is locked
     *
     * @param userid User ID
     * @param remoteip Client IP address
     * @return true if account is locked, false otherwise
     * @throws ServletException if a servlet error occurs
     */
    public static boolean isAccountLocked(String userid, String remoteip) throws ServletException
    {
        if (userid == null || remoteip == null) {
            throw new ServletException("Invalid parameters: userid or remoteip is null");
        }

        boolean isLocked = false;
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();

            // Query account lock status
            String sql = "SELECT islocked FROM users WHERE userid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userid);

            rs = stmt.executeQuery();

            if (rs.next()) {
                isLocked = (rs.getInt("islocked") == 1);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error checking account lock status: " + e.getMessage(), e);
            throw new ServletException("Database error checking account lock status", e);
        } finally {
            // Close resources
            DatabaseUtil.closeResources(conn, stmt, rs);
        }

        return isLocked;
    }

    /**
     * Reset the failed login counts of a user to zero 
     * If an account is locked an exception will be thrown
     *
     * @param userid User ID
     * @param remoteip Client IP address
     * @throws ServletException if a servlet error occurs
     */
    public static void resetFailLogin(String userid, String remoteip) throws ServletException
    {
        if (userid == null || remoteip == null) {
            throw new ServletException("Invalid parameters: userid or remoteip is null");
        }

        // Check if account is locked
        if (isAccountLocked(userid, remoteip)) {
            throw new ServletException("Cannot reset fail login count for locked account: " + userid);
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();

            // Reset fail login count
            String sql = "UPDATE users SET faillogin = 0 WHERE userid = ?";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userid);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                log.info("Reset fail login count: " + userid + " - " + remoteip);
            } else {
                log.warning("Failed to reset fail login count: " + userid + " - " + remoteip);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error resetting fail login count: " + e.getMessage(), e);
            throw new ServletException("Database error resetting fail login count", e);
        } finally {
            // Close resources
            DatabaseUtil.closeResources(conn, stmt, null);
        }
    }
}