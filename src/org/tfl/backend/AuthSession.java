package org.tfl.backend;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;


public class AuthSession
{

    private static final Logger log = Logger.getLogger(AuthSession.class.getName());
    
    /**
     * Validate if a session has been authenticated successfully and is still valid
     * Redirect to login page if session is not authenticated or invalid
     * 
     * @param req
     * @param resp
     * @return true if session is authenticated successfully, false otherwise
     * @throws IOException
     * @throws ServletException
     */
    public static boolean validate(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException
    {
        if (req == null || resp == null)
        {
        	//TODO Throw Exception
        }

      
        HttpSession sess = req.getSession(false);
        
        if(sess == null)
        {
        	//TODO Redirect index.jsp
        }
        
       
        if (sess.getAttribute("userid") == null)
        { // not authenticated
        	//TODO Redirect index.jsp
        }
        
        return true;        
    }
    
    
   
    
    /**
     * Check if 2fa userid attribute is set. If it is not, redirect to specified error url
     * 
     * @param req
     * @param resp
     * @param redirecturl
     * @return true if 2fa userid attribute is properly set, false otherwise
     * @throws IOException
     * @throws ServletException
     */
    public static boolean check2FASession(HttpServletRequest req, HttpServletResponse resp, String redirecturl)
            throws IOException, ServletException
    {
        if (req == null || resp == null || redirecturl == null)
        {
        	//TODO Throw Exception
        }

        HttpSession session = req.getSession(false);
        
        if(session == null)
        {
        	//TODO Redirect redirecturl
        }
       
        String userid2fa = (String) session.getAttribute("userid2fa");
        if (userid2fa == null)
        {
        	//TODO Redirect redirecturl
        }
        
        return true;
    }
}

