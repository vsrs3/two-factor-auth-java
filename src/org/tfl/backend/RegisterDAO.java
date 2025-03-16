package org.tfl.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.tfl.crypto.CryptoUtil;

public class RegisterDAO {

	private static final Logger log = Logger.getLogger(RegisterDAO.class.getName());

	/**
	 * Check user existence
	 *
	 * @param userid
	 * @param remoteip client ip address
	 * @return true if user is existent, false otherwise
	 */
	public static boolean findUser(String userid, String remoteip) {
		boolean isFound = false;

		// Input validation
		if (userid == null || remoteip == null) {
			log.warning("Invalid parameters: userid or remoteip is null - " + remoteip);
			return false;
		}

		Connection conn = null;
		PreparedStatement stmt = null;
		ResultSet rs = null;

		try {
			// Get database connection (implementation depends on your DB setup)
			conn = DatabaseUtil.getConnection();

			// Prepare SQL query to check if user exists
			String sql = "SELECT userid FROM users WHERE userid = ?";
			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userid);

			// Execute query
			rs = stmt.executeQuery();

			// If result set has at least one row, user exists
			if (rs.next()) {
				isFound = true;
				log.info("User found: " + userid + " - " + remoteip);
			}

		} catch (SQLException e) {
			log.log(Level.SEVERE, "Database error checking user existence: " + e.getMessage(), e);
		} finally {
			// Close resources
			DatabaseUtil.closeResources(conn, stmt, rs);
		}

		return isFound;
	}

	/**
	 * Add a new user to the database
	 *
	 * @param firstName First name of user
	 * @param lastName Last name of user
	 * @param userid User ID (username)
	 * @param password User's password (plaintext, will be hashed)
	 * @param salt Salt for password hashing
	 * @param otpSecret Secret for OTP generation
	 * @param remoteip Client IP address
	 * @return true if user added successfully, false otherwise
	 */
	public static boolean addUser(String firstName, String lastName, String userid,
								  String password, String salt, String otpSecret, String remoteip) {
		boolean isSuccess = false;

		// Input validation
		if (firstName == null || lastName == null || userid == null ||
				password == null || salt == null || otpSecret == null || remoteip == null) {
			log.warning("Invalid parameters for user registration - " + remoteip);
			return false;
		}

		Connection conn = null;
		PreparedStatement stmt = null;

		try {
			// Get database connection
			conn = DatabaseUtil.getConnection();

			// Hash password with salt using PBKDF2
			char[] passwordChars = password.toCharArray();
			byte[] saltBytes = CryptoUtil.hexStringToByteArray(salt);
			byte[] hashedPassword = CryptoUtil.getPasswordKey(passwordChars, saltBytes, CryptoUtil.PBE_ITERATION);
			String passwordHash = CryptoUtil.byteArrayToHexString(hashedPassword);

			// Zero out the password array for security
			CryptoUtil.zeroCharArray(passwordChars);

			// Initial security label (default: Unclassified - 1)
			int securityLabel = 1;

			// Prepare SQL for insertion
			String sql = "INSERT INTO users (userid, firstname, lastname, salt, password, otpsecret, islocked, faillogin, label) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

			stmt = conn.prepareStatement(sql);
			stmt.setString(1, userid);
			stmt.setString(2, firstName);
			stmt.setString(3, lastName);
			stmt.setString(4, salt);
			stmt.setString(5, passwordHash);
			stmt.setString(6, otpSecret);
			stmt.setInt(7, 0);  // Not locked
			stmt.setInt(8, 0);  // No failed logins
			stmt.setInt(9, securityLabel);  // Default security level

			// Execute insert
			int rowsAffected = stmt.executeUpdate();

			// If one row was affected, insertion was successful
			if (rowsAffected == 1) {
				isSuccess = true;
				log.info("User registered successfully: " + userid + " - " + remoteip);
			}

		} catch (SQLException e) {
			log.log(Level.SEVERE, "Database error adding user: " + e.getMessage(), e);
		} finally {
			// Close resources
			DatabaseUtil.closeResources(conn, stmt, null);
		}

		return isSuccess;
	}
}