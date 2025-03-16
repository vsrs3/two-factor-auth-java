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
 * Servlet implementation class LoginServlet
 * Handles user login first authentication step (username/password)
 */
@WebServlet("/login")
public class LoginControllerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(LoginControllerServlet.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public LoginControllerServlet()
    {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);
        if(session == null) {
            // No existing session
            log.warning("No session found for login - " + request.getRemoteAddr());
            response.sendRedirect("/index.jsp");
            return;
        }

        // Get login parameters
        String userid = request.getParameter("userid");
        String password = request.getParameter("password");

        // Check if required parameters are present
        if (userid == null || password == null) {
            log.warning("Missing login parameters - " + request.getRemoteAddr());
            response.sendRedirect("/index.jsp");
            return;
        }

        try {
            // Check if account is locked
            if (LoginDAO.isAccountLocked(userid, request.getRemoteAddr())) {
                log.warning("Login attempt on locked account: " + userid + " - " + request.getRemoteAddr());
                response.sendRedirect("/locked.html");
                return;
            }

            // Validate user credentials
            if (LoginDAO.validateUser(userid, password, request.getRemoteAddr())) {
                // Clear sensitive data
                password = null;

                // Prevent session fixation, invalidate and assign a new session
                session.invalidate();
                session = request.getSession(true);
                session.setAttribute(AppConstants.SESSION_USERID_2FA, userid);

                // Set the session id cookie with HttpOnly, secure and samesite flags
                String custsession = "JSESSIONID=" + session.getId() + ";Path=/;Secure;HttpOnly;SameSite=Strict";
                response.setHeader("Set-Cookie", custsession);

                // Dispatch request to OTP page
                RequestDispatcher dispatcher = request.getRequestDispatcher("/otp.jsp");
                dispatcher.forward(request, response);
            } else {
                // Invalid credentials
                log.warning("Invalid login credentials: " + userid + " - " + request.getRemoteAddr());

                // Add error message to session
                session.setAttribute("loginError", "Invalid username or password");

                // Increment fail login count (handled by validateUser)
                response.sendRedirect("/index.jsp");
            }
        } catch (ServletException e) {
            log.severe("Error during login: " + e.getMessage());
            response.sendRedirect("/error.html");
        }
    }
}