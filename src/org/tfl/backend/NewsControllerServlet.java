package org.tfl.backend;

import org.tfl.backend.NewsDAO.NewsItem;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Servlet for handling news posting and retrieval with Bell-Lapadula security model
 */
@WebServlet("/news")
public class NewsControllerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(NewsControllerServlet.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public NewsControllerServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
     * Handles viewing news with Bell-Lapadula No-Read-Up policy
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Verify user is authenticated
        if (!AuthSession.validate(request, response)) {
            return; // validate handles the redirect
        }

        // Get the current user from session
        HttpSession session = request.getSession(false);
        String userid = (String) session.getAttribute(AppConstants.SESSION_USERID);

        // Get news items according to user's security level (Bell-Lapadula No-Read-Up)
        List<NewsItem> newsList = NewsDAO.getNewsForUser(userid, request.getRemoteAddr());

        // Store news items in request attribute
        request.setAttribute("newsList", newsList);

        // Get user's security level for display
        int userSecurityLevel = UserDAO.getUserSecurityLevel(userid, request.getRemoteAddr());
        String securityLevelName = NewsDAO.getSecurityLabelName(userSecurityLevel);
        request.setAttribute("userSecurityLevel", userSecurityLevel);
        request.setAttribute("securityLevelName", securityLevelName);

        // Forward to news viewing page
        RequestDispatcher dispatcher = request.getRequestDispatcher("/news.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     * Handles posting news with Bell-Lapadula No-Write-Down policy
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        // Rest of the method remains unchanged
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        // Verify user is authenticated
        if (!AuthSession.validate(request, response)) {
            return; // validate handles the redirect
        }

        // Get the current user from session
        HttpSession session = request.getSession(false);
        String userid = (String) session.getAttribute(AppConstants.SESSION_USERID);

        // Get parameters
        String content = request.getParameter("content");
        String securityLevelStr = request.getParameter("securitylevel");

        // Validate parameters
        if (content == null || securityLevelStr == null) {
            log.warning("Missing parameters for news post - " + request.getRemoteAddr());
            request.setAttribute("postError", "Missing required parameters");
            doGet(request, response); // Show news page with error message
            return;
        }

        // Validate content
        if (content.trim().isEmpty()) {
            log.warning("Empty content for news post - " + request.getRemoteAddr());
            request.setAttribute("postError", "Content cannot be empty");
            doGet(request, response); // Show news page with error message
            return;
        }

        // Parse security level
        int securityLevel;
        try {
            securityLevel = Integer.parseInt(securityLevelStr);
            if (securityLevel < AppConstants.SECURITY_UNCLASSIFIED ||
                    securityLevel > AppConstants.SECURITY_TOP_SECRET) {
                throw new NumberFormatException("Invalid security level: " + securityLevelStr);
            }
        } catch (NumberFormatException e) {
            log.warning("Invalid security level: " + securityLevelStr + " - " + request.getRemoteAddr());
            request.setAttribute("postError", "Invalid security level");
            doGet(request, response); // Show news page with error message
            return;
        }

        // Get user's security level for Bell-Lapadula check
        int userSecurityLevel = UserDAO.getUserSecurityLevel(userid, request.getRemoteAddr());

        // Bell-Lapadula No-Write-Down policy: User can post at or above their level
        if (securityLevel < userSecurityLevel) {
            log.warning("Bell-Lapadula violation - No-Write-Down: " + userid +
                    " (level " + userSecurityLevel + ") tried to post at level " +
                    securityLevel + " - " + request.getRemoteAddr());
            request.setAttribute("postError", "Security violation: You cannot post at a lower security level than your own.");
            doGet(request, response); // Show news page with error message
            return;
        }

        // Post the news item
        boolean success = NewsDAO.addNews(userid, content, securityLevel, request.getRemoteAddr());

        if (success) {
            log.info("News posted by " + userid + " at security level " + securityLevel +
                    " - " + request.getRemoteAddr());
            request.setAttribute("postSuccess", "News posted successfully");
        } else {
            log.warning("Failed to post news: " + userid + " - " + request.getRemoteAddr());
            request.setAttribute("postError", "Failed to post news");
        }

        // Show news page with result message
        doGet(request, response);
    }
}