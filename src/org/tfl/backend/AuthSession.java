package org.tfl.backend;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;

/**
 * Manages authentication session validation
 */
public class AuthSession
{
    private static final Logger log = Logger.getLogger(AuthSession.class.getName());

    /**
     * Validate if a session has been authenticated successfully and is still valid
     * Redirect to login page if session is not authenticated or invalid
     *
     * @param req HTTP request
     * @param resp HTTP response
     * @return true if session is authenticated successfully, false otherwise
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    public static boolean validate(HttpServletRequest req, HttpServletResponse resp)
            throws IOException, ServletException
    {
        if (req == null || resp == null)
        {
            log.severe("Invalid parameters: request or response is null");
            throw new ServletException("Invalid parameters: request or response is null");
        }

        HttpSession sess = req.getSession(false);

        if (sess == null)
        {
            // No session exists, redirect to login page
            log.warning("No session found - " + req.getRemoteAddr());
            resp.sendRedirect("/index.jsp");
            return false;
        }

        if (sess.getAttribute(AppConstants.SESSION_USERID) == null)
        {
            // Not authenticated
            log.warning("Session not authenticated - " + req.getRemoteAddr());
            resp.sendRedirect("/index.jsp");
            return false;
        }

        return true;
    }

    /**
     * Check if 2fa userid attribute is set. If it is not, redirect to specified error url
     *
     * @param req HTTP request
     * @param resp HTTP response
     * @param redirecturl URL to redirect to if validation fails
     * @return true if 2fa userid attribute is properly set, false otherwise
     * @throws IOException if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    public static boolean check2FASession(HttpServletRequest req, HttpServletResponse resp, String redirecturl)
            throws IOException, ServletException
    {
        if (req == null || resp == null || redirecturl == null)
        {
            log.severe("Invalid parameters: request, response, or redirecturl is null");
            throw new ServletException("Invalid parameters: request, response, or redirecturl is null");
        }

        HttpSession session = req.getSession(false);

        if (session == null)
        {
            // No session exists, redirect to specified URL
            log.warning("No session found for 2FA check - " + req.getRemoteAddr());
            resp.sendRedirect(redirecturl);
            return false;
        }

        String userid2fa = (String) session.getAttribute(AppConstants.SESSION_USERID_2FA);
        if (userid2fa == null)
        {
            // 2FA userid not set, redirect to specified URL
            log.warning("No 2FA userid in session - " + req.getRemoteAddr());
            resp.sendRedirect(redirecturl);
            return false;
        }

        return true;
    }
}