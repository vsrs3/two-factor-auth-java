package org.tfl.backend;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Data Access Object for news operations implementing Bell-Lapadula model
 */
public class NewsDAO {

    private static final Logger log = Logger.getLogger(NewsDAO.class.getName());

    /**
     * Model for news items
     */
    public static class NewsItem {
        private int id;
        private String userid;
        private String content;
        private Date date;
        private int securityLabel;

        public int getId() { return id; }
        public void setId(int id) { this.id = id; }

        public String getUserid() { return userid; }
        public void setUserid(String userid) { this.userid = userid; }

        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }

        public Date getDate() { return date; }
        public void setDate(Date date) { this.date = date; }

        public int getSecurityLabel() { return securityLabel; }
        public void setSecurityLabel(int securityLabel) { this.securityLabel = securityLabel; }
    }

    /**
     * Add a news item with Bell-Lapadula security constraints
     * No-Read-Up, No-Write-Down policy
     *
     * @param userid User posting the news
     * @param content News content
     * @param securityLabel Security label for the news
     * @param remoteip Client IP address
     * @return true if successful, false otherwise
     */
    public static boolean addNews(String userid, String content, int securityLabel, String remoteip) {
        if (userid == null || content == null || remoteip == null ||
                securityLabel < AppConstants.SECURITY_UNCLASSIFIED ||
                securityLabel > AppConstants.SECURITY_TOP_SECRET) {
            log.warning("Invalid parameters for addNews - " + remoteip);
            return false;
        }

        // Get user's security level
        int userSecurityLevel = UserDAO.getUserSecurityLevel(userid, remoteip);

        if (userSecurityLevel == 0) {
            log.warning("User not found for addNews: " + userid + " - " + remoteip);
            return false;
        }

        // Bell-Lapadula No-Write-Down: Users can only write content at their security level or higher
        if (securityLabel < userSecurityLevel) {
            log.warning("Bell-Lapadula violation - No-Write-Down: " + userid +
                    " (level " + userSecurityLevel + ") tried to post at level " +
                    securityLabel + " - " + remoteip);
            return false;
        }

        boolean success = false;
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();

            // Insert news item
            String sql = "INSERT INTO news (userid, content, date, label) VALUES (?, ?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, userid);
            stmt.setString(2, content);
            stmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            stmt.setInt(4, securityLabel);

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected == 1) {
                success = true;
                log.info("News posted: " + userid + " (security level: " + securityLabel + ") - " + remoteip);
            }

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error adding news: " + e.getMessage(), e);
        } finally {
            // Close resources
            DatabaseUtil.closeResources(conn, stmt, null);
        }

        return success;
    }

    /**
     * Get news accessible to a user according to Bell-Lapadula model
     * No-Read-Up policy: users can only read content at or below their security level
     *
     * @param userid User requesting news
     * @param remoteip Client IP address
     * @return List of accessible news items or empty list if none found
     */
    public static List<NewsItem> getNewsForUser(String userid, String remoteip) {
        List<NewsItem> newsList = new ArrayList<>();

        if (userid == null || remoteip == null) {
            log.warning("Invalid parameters for getNewsForUser - " + remoteip);
            return newsList;
        }

        // Get user's security level
        int userSecurityLevel = UserDAO.getUserSecurityLevel(userid, remoteip);

        if (userSecurityLevel == 0) {
            log.warning("User not found for getNewsForUser: " + userid + " - " + remoteip);
            return newsList;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Get database connection
            conn = DatabaseUtil.getConnection();

            // Bell-Lapadula No-Read-Up: Users can only read content at or below their security level
            String sql = "SELECT id, userid, content, date, label FROM news WHERE label <= ? ORDER BY date DESC";
            stmt = conn.prepareStatement(sql);
            stmt.setInt(1, userSecurityLevel);

            rs = stmt.executeQuery();

            while (rs.next()) {
                NewsItem newsItem = new NewsItem();
                newsItem.setId(rs.getInt("id"));
                newsItem.setUserid(rs.getString("userid"));
                newsItem.setContent(rs.getString("content"));
                newsItem.setDate(new Date(rs.getTimestamp("date").getTime()));
                newsItem.setSecurityLabel(rs.getInt("label"));

                newsList.add(newsItem);
            }

            log.info("Retrieved " + newsList.size() + " news items for user: " +
                    userid + " (security level: " + userSecurityLevel + ") - " + remoteip);

        } catch (SQLException e) {
            log.log(Level.SEVERE, "Database error retrieving news: " + e.getMessage(), e);
        } finally {
            // Close resources
            DatabaseUtil.closeResources(conn, stmt, rs);
        }

        return newsList;
    }

    /**
     * Get the security label name for a security level
     *
     * @param securityLevel The security level (1-4)
     * @return The security label name or "Unknown" if invalid
     */
    public static String getSecurityLabelName(int securityLevel) {
        switch (securityLevel) {
            case AppConstants.SECURITY_UNCLASSIFIED:
                return "Unclassified";
            case AppConstants.SECURITY_CONFIDENTIAL:
                return "Confidential";
            case AppConstants.SECURITY_SECRET:
                return "Secret";
            case AppConstants.SECURITY_TOP_SECRET:
                return "Top Secret";
            default:
                return "Unknown";
        }
    }
}