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

import org.tfl.backend.AuthSession;
import org.tfl.crypto.CryptoUtil;
import org.tfl.crypto.TimeBaseOTP;

/**
 * Servlet implementation class OTPControllerServlet
 * Handles OTP validation for two-factor authentication
 */
@WebServlet("/otpctl")
public class OTPControllerServlet extends HttpServlet
{
    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(OTPControllerServlet.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public OTPControllerServlet()
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

        // Make sure it has a valid 2fa session from login page
        // userid2fa session attribute must be set
        if (!AuthSession.check2FASession(request, response, "/index.jsp")) {
            return; // check2FASession handles the redirection
        }

        // Get current session
        HttpSession session = request.getSession(false);
        String userid = (String) session.getAttribute(AppConstants.SESSION_USERID_2FA);

        // Remove the userid2fa attribute to prevent multiple submission attempts
        session.removeAttribute(AppConstants.SESSION_USERID_2FA);

        // Get OTP value from request
        String otpvalue = (String) request.getParameter("totp");

        if (otpvalue == null)
        {
            log.warning("OTP value not provided - " + request.getRemoteAddr());
            session.invalidate();
            response.sendRedirect("/error.html");
            return;
        }

        // Get OTP secret from database
        String otpsecret = null;
        try {
            otpsecret = OTPDAO.getOTPSecret(userid, request.getRemoteAddr());
        } catch (ServletException e) {
            log.severe("Error retrieving OTP secret: " + e.getMessage());
            session.invalidate();
            response.sendRedirect("/error.html");
            return;
        }

        if (otpsecret == null) {
            log.warning("OTP secret not found for user: " + userid);
            session.invalidate();
            response.sendRedirect("/error.html");
            return;
        }

        // Generate OTP using TimeBaseOTP class
        String otpresult = TimeBaseOTP.generateOTP(CryptoUtil.hexStringToByteArray(otpsecret));

        // Clear sensitive data
        otpsecret = null;

        if (otpresult == null)
        {
            log.severe("Failed to generate OTP - " + request.getRemoteAddr());
            session.invalidate();
            response.sendRedirect("/error.html");
            return;
        }

        if (otpresult.equals(otpvalue))
        {
            // Correct OTP value
            log.info("OTP validation successful: " + userid + " - " + request.getRemoteAddr());

            try {
                // Reset fail login attempts
                OTPDAO.resetFailLogin(userid, request.getRemoteAddr());
            } catch (ServletException e) {
                log.warning("Error resetting fail login count: " + e.getMessage());
                // Continue anyway since authentication was successful
            }

            // Create new session for authenticated user
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute(AppConstants.SESSION_USERID, userid);
            session.setAttribute(AppConstants.SESSION_ANTICSRF, "AntiCSRF");

            // Set secure session cookie
            String custsession = "JSESSIONID=" + session.getId() + ";Path=/;Secure;HttpOnly;SameSite=Strict";
            response.setHeader("Set-Cookie", custsession);

            // Redirect to success page
            response.sendRedirect("/success.jsp");
        }
        else
        {
            // Incorrect OTP value
            String remoteip = request.getRemoteAddr();
            log.warning("Error: Invalid OTP value " + remoteip + " " + userid);

            try {
                // Update fail login count
                LoginDAO.incrementFailLogin(userid, remoteip);

                // Check if account is now locked
                if (LoginDAO.isAccountLocked(userid, remoteip)) {
                    log.warning("Account locked after OTP failures: " + userid + " - " + remoteip);
                    session.invalidate();
                    response.sendRedirect("/locked.html");
                } else {
                    // Not locked, allow retry
                    session = request.getSession(true);
                    session.setAttribute(AppConstants.SESSION_USERID_2FA, userid);
                    session.setAttribute(AppConstants.SESSION_OTP_ERROR, "Invalid OTP code. Please try again.");

                    // Return to OTP page
                    if (request.getRequestURI().contains("confirm.jsp")) {
                        response.sendRedirect("/confirm.jsp");
                    } else {
                        response.sendRedirect("/otp.jsp");
                    }
                }
            } catch (ServletException e) {
                log.severe("Error updating login failure: " + e.getMessage());
                session.invalidate();
                response.sendRedirect("/error.html");
            }
        }
    }
}