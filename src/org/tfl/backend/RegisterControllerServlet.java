package org.tfl.backend;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tfl.crypto.CryptoUtil;

/**
 * Servlet implementation class RegisterServlet
 * Handles user registration process
 */
@WebServlet("/register")
public class RegisterControllerServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(RegisterControllerServlet.class.getName());

    // Pattern for password validation (at least one uppercase and one lowercase)
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z]).*$");

    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterControllerServlet() {
        super();
    }

    /**
     * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
     *      response)
     */
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);
        if(session == null) {
            // No existing session
            log.warning("No session found for registration - " + request.getRemoteAddr());
            response.sendRedirect("/index.jsp");
            return;
        }

        // Get registration parameters
        String firstName = request.getParameter("firstname");
        String lastName = request.getParameter("lastname");
        String userid = request.getParameter("userid");
        String password = request.getParameter("password");
        String repassword = request.getParameter("repassword");

        // Check if all required parameters are present
        if (firstName == null || lastName == null || userid == null ||
                password == null || repassword == null) {
            log.warning("Missing registration parameters - " + request.getRemoteAddr());
            response.sendRedirect("/index.jsp");
            return;
        }

        // Validate password length
        if (password.length() < AppConstants.MIN_LENGTH_PASS) {
            log.warning("Password too short: " + userid + " - " + request.getRemoteAddr());
            response.sendRedirect("/error.html");
            return;
        }

        // Validate password format (uppercase and lowercase)
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            log.warning("Password doesn't meet complexity requirements: " +
                    userid + " - " + request.getRemoteAddr());
            response.sendRedirect("/error.html");
            return;
        }

        // Validate passwords match
        if (!password.equals(repassword)) {
            log.warning("Passwords don't match: " + userid + " - " + request.getRemoteAddr());
            response.sendRedirect("/error.html");
            return;
        }

        // Check if user already exists
        if (RegisterDAO.findUser(userid, request.getRemoteAddr())) {
            log.warning("User already exists: " + userid + " - " + request.getRemoteAddr());
            response.sendRedirect("/error.html");
            return;
        } else {
            // Generate random salt
            byte[] saltBytes = CryptoUtil.generateRandomBytes(CryptoUtil.SALT_SIZE);
            String salt = CryptoUtil.byteArrayToHexString(saltBytes);

            // Generate OTP Secret (20 bytes is standard for TOTP)
            String otpSecret = CryptoUtil.genHexaOTPSecret();

            // Add new user to database
            boolean success = RegisterDAO.addUser(firstName, lastName, userid,
                    password, salt, otpSecret,
                    request.getRemoteAddr());

            if (!success) {
                log.severe("Failed to register user: " + userid + " - " + request.getRemoteAddr());
                response.sendRedirect("/error.html");
                return;
            }

            // Clear sensitive data
            password = null;
            repassword = null;

            // Prevent session fixation, invalidate and assign a new session
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute(AppConstants.SESSION_USERID_2FA, userid);

            // Set the session id cookie with HttpOnly, secure and samesite flags
            String custsession = "JSESSIONID=" + session.getId() + ";Path=/;Secure;HttpOnly;SameSite=Strict";
            response.setHeader("Set-Cookie", custsession);

            // Dispatch request to confirm.jsp for OTP setup
            RequestDispatcher dispatcher = request.getRequestDispatcher("/confirm.jsp");
            dispatcher.forward(request, response);
        }
    }
}