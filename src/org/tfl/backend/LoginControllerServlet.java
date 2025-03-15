package org.tfl.backend;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * Servlet implementation class LoginServlet
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);
        if(session == null)
        {//no existing session
        	
        	//TODO Redirect to index.jsp
       
        }
        

        String userid = request.getParameter("userid");
        String password = request.getParameter("password");

        if (userid == null || password == null)
        {
        	
        	//TODO Redirect to index.jsp 
        
        }
        
        if(LoginDAO.isAccountLocked(userid, request.getRemoteAddr()))
        {
            log.warning("Error: Account is locked " + userid  + " " + request.getRemoteAddr());
            response.sendRedirect("/index.jsp");
            
        }

        else if (LoginDAO.validateUser(userid, password, request.getRemoteAddr()))
        {
            password = null;
            //Prevent Session fixation, invalidate and assign a new session
            
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("userid2fa", userid);
            //Set the session id cookie with HttpOnly, secure and samesite flags
            String custsession = "JSESSIONID=" + session.getId() + ";Path=/;Secure;HttpOnly;SameSite=Strict";
            response.setHeader("Set-Cookie", custsession);
            
            //Dispatch request to otp.jsp

        }
        else
        {	
        	log.warning("Error: Username or password is invalid " + userid  + " " + request.getRemoteAddr());
        	String remoteip = request.getRemoteAddr();
        	LoginDAO.incrementFailLogin(userid, remoteip);
        	response.sendRedirect("/index.jsp");
        }

    }

}
