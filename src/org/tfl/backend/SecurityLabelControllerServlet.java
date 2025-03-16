package org.tfl.backend;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet for handling security label assignments
 * Restricted to admin users only
 */
@WebServlet("/securitylabel")
public class SecurityLabelControllerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(SecurityLabelControllerServlet.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public SecurityLabelControllerServlet() {
        super();
    }

    /**
     * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
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

        // Check if user is admin
        if (!"admin".equals(userid)) {
            log.warning("Non-admin user attempted to access security label page: " +
                    userid + " - " + request.getRemoteAddr());
            response.sendRedirect("/error.html");
            return;
        }

        // Forward to security label assignment page
        RequestDispatcher dispatcher = request.getRequestDispatcher("/securitylabel.jsp");
        dispatcher.forward(request, response);
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        // Verify user is authenticated
        if (!AuthSession.validate(request, response)) {
            return; // validate handles the redirect
        }

        // Get the current user from session
        HttpSession session = request.getSession(false);
        String adminId = (String) session.getAttribute(AppConstants.SESSION_USERID);

        // Check if user is admin
        if (!"admin".equals(adminId)) {
            log.warning("Non-admin user attempted to set security label: " +
                    adminId + " - " + request.getRemoteAddr());
            response.sendRedirect("/error.html");
            return;
        }

        // Get parameters
        String targetUserId = request.getParameter("targetuser");
        String securityLevelStr = request.getParameter("securitylevel");

        // Validate parameters
        if (targetUserId == null || securityLevelStr == null) {
            log.warning("Missing parameters for security label assignment - " + request.getRemoteAddr());
            request.setAttribute("errorMsg", "Missing required parameters");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/securitylabel.jsp");
            dispatcher.forward(request, response);
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
            request.setAttribute("errorMsg", "Invalid security level");
            RequestDispatcher dispatcher = request.getRequestDispatcher("/securitylabel.jsp");
            dispatcher.forward(request, response);
            return;
        }

        // Update user security level
        boolean success = UserDAO.setUserSecurityLevel(adminId, targetUserId, securityLevel,
                request.getRemoteAddr());

        if (success) {
            log.info("Security level updated for " + targetUserId + " to " + securityLevel +
                    " by " + adminId + " - " + request.getRemoteAddr());
            request.setAttribute("successMsg", "Security level updated successfully");
        } else {
            log.warning("Failed to update security level for " + targetUserId +
                    " - " + request.getRemoteAddr());
            request.setAttribute("errorMsg", "Failed to update security level. User may not exist.");
        }

        // Return to security label page
        RequestDispatcher dispatcher = request.getRequestDispatcher("/securitylabel.jsp");
        dispatcher.forward(request, response);
    }
}