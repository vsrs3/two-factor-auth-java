package org.tfl.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;

import org.tfl.crypto.CryptoUtil;

/**
 * Data Access Object for login operations
 */
public class LoginDAO
{
	private static final Logger log = Logger.getLogger(LoginDAO.class.getName());

	/**
	 * Validates user credentials
	 *
	 * @param userid User ID
	 * @param password Password to validate
	 * @param remoteip Client IP address
	 * @return true if user is valid, false otherwise
	 */
	public static boolean validateUser(String userid, String password, String remoteip)
	{
		boolean isValid = false;

		// Input validation
		if (userid == null || password == null || remoteip == null) {
			log.warning("Invalid parameters for validateUser - " + remoteip);
			return false;
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Get database connection
			conn = DatabaseUtil.getConnection();

			// Query user record
			String sql = "SELECT userid, password, salt, islocked, faillogin FROM users WHERE userid = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userid);

			rs = stmt.executeQuery();

			if (rs.next()) {
				// User exists, check if account is locked
				int isLocked = rs.getInt("islocked");

				if (isLocked == 0) {
					// Account is not locked, validate password
					String storedPasswordHash = rs.getString("password");
					String salt = rs.getString("salt");

					// Hash password with stored salt
					char[] passwordChars = password.toCharArray();
					byte[] saltBytes = CryptoUtil.hexStringToByteArray(salt);
					byte[] calculatedHash = CryptoUtil.getPasswordKey(
							passwordChars, saltBytes, CryptoUtil.PBE_ITERATION);
					String calculatedHashString = CryptoUtil.byteArrayToHexString(calculatedHash);

					// Zero out password characters for security
					CryptoUtil.zeroCharArray(passwordChars);

					// Compare calculated hash with stored hash
					if (storedPasswordHash.equals(calculatedHashString)) {
						isValid = true;
						// Password matches, reset fail login count
						resetFailLogin(userid, remoteip);
						log.info("User authenticated successfully: " + userid + " - " + remoteip);
					} else {
						// Incorrect password, increment fail login count
						int currentFailCount = rs.getInt("faillogin");
						incrementFailLoginCount(userid, currentFailCount, remoteip);
						log.warning("Invalid password: " + userid + " - " + remoteip);
					}
				} else {
					// Account is locked
					log.warning("Attempted login to locked account: " + userid + " - " + remoteip);
				}
			} else {
				// User does not exist, log attempt
				log.warning("Login attempt for non-existent user: " + userid + " - " + remoteip);
			}

		} catch (SQLException e) {
			log.log(Level.SEVERE, "Database error validating user: " + e.getMessage(), e);
		} catch (ServletException e) {
			log.log(Level.SEVERE, "Servlet error validating user: " + e.getMessage(), e);
		} finally {
			// Close resources
			DatabaseUtil.closeResources(conn, stmt, rs);
		}

		return isValid;
	}

	/**
	 * Check if a user account is locked
	 *
	 * @param userid User ID
	 * @param remoteip Client IP address
	 * @return true if account is locked or false otherwise
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

			// Query user record
			String sql = "SELECT islocked FROM users WHERE userid = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userid);

			rs = stmt.executeQuery();

			if (rs.next()) {
				// User exists, check if account is locked
				int lockedValue = rs.getInt("islocked");
				isLocked = (lockedValue == 1);

				if (isLocked) {
					log.info("Account is locked: " + userid + " - " + remoteip);
				}
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
	 * Increments the failed login count for a user
	 * Locks the user account if fail logins exceed threshold.
	 *
	 * @param userid User ID
	 * @param remoteip Client IP address
	 * @throws ServletException if a servlet error occurs
	 */
	public static void incrementFailLogin(String userid, String remoteip)
			throws ServletException
	{
		if (userid == null || remoteip == null) {
			throw new ServletException("Invalid parameters: userid or remoteip is null");
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Get database connection
			conn = DatabaseUtil.getConnection();

			// Get current fail count
			String sql = "SELECT faillogin FROM users WHERE userid = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userid);

			rs = stmt.executeQuery();

			if (rs.next()) {
				int currentFailCount = rs.getInt("faillogin");
				incrementFailLoginCount(userid, currentFailCount, remoteip);
			}

		} catch (SQLException e) {
			log.log(Level.SEVERE, "Database error incrementing fail login: " + e.getMessage(), e);
			throw new ServletException("Database error incrementing fail login", e);
		} finally {
			// Close resources
			DatabaseUtil.closeResources(conn, stmt, rs);
		}
	}

	/**
	 * Helper method to increment fail login count and lock account if needed
	 *
	 * @param userid User ID
	 * @param currentFailCount Current failure count
	 * @param remoteip Client IP address
	 * @throws ServletException if a servlet error occurs
	 */
	private static void incrementFailLoginCount(String userid, int currentFailCount, String remoteip)
			throws ServletException {

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			// Get database connection
			conn = DatabaseUtil.getConnection();

			// Increment fail count
			int newFailCount = currentFailCount + 1;
			boolean shouldLock = (newFailCount >= AppConstants.MAX_FAIL_LOGIN);

			// Update user record
			String sql = "UPDATE users SET faillogin = ?, islocked = ? WHERE userid = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setInt(1, newFailCount);
			stmt.setInt(2, shouldLock ? 1 : 0);
			stmt.setString(3, userid);

			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected == 1) {
				if (shouldLock) {
					log.warning("Account locked after too many failed attempts: " +
							userid + " - " + remoteip);
				} else {
					log.info("Fail login count incremented: " + userid +
							" (count=" + newFailCount + ") - " + remoteip);
				}
			}

		} catch (SQLException e) {
			log.log(Level.SEVERE, "Database error updating fail login count: " + e.getMessage(), e);
			throw new ServletException("Database error updating fail login count", e);
		} finally {
			// Close resources
			DatabaseUtil.closeResources(conn, stmt, null);
		}
	}

	/**
	 * Reset the failed login count for a user
	 *
	 * @param userid User ID
	 * @param remoteip Client IP address
	 * @throws ServletException if a servlet error occurs
	 */
	private static void resetFailLogin(String userid, String remoteip)
			throws ServletException {

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			// Get database connection
			conn = DatabaseUtil.getConnection();

			// Reset fail count
			String sql = "UPDATE users SET faillogin = 0 WHERE userid = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userid);

			int rowsAffected = stmt.executeUpdate();

			if (rowsAffected == 1) {
				log.info("Reset fail login count: " + userid + " - " + remoteip);
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