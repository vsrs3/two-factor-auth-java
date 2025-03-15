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
 * Servlet implementation class RegisterServlet
 */
@WebServlet("/register")
public class RegisterControllerServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(LoginControllerServlet.class.getName());

    /**
     * @see HttpServlet#HttpServlet()
     */
    public RegisterControllerServlet()
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
        String firstName = request.getParameter("firstname");
        String lastName = request.getParameter("lastname");
        String userid = request.getParameter("userid");
        String password = request.getParameter("password");
        String repassword = request.getParameter("repassword");
        
        if (firstName == null || lastName == null || userid == null || password == null || repassword == null)
        {
        	//TODO Redirect to index.jsp 
        }
        
        if (password.length() < AppConstants.MIN_LENGTH_PASS) {
        	//TODO Redirect to error.html
        }
        
        if (password.compareTo(repassword) != 0)
        {
        	//TODO Redirect to error.html 
        }
        
        if (RegisterDAO.findUser(userid, request.getRemoteAddr()))
        {
        	//TODO Redirect to error.html

        }
        else
        {
            //TODO Generate salt by CryptoUtil
        	
        	//TODO Encode Base64 salt
        	
        	//TODO	Generate hexadecimal OTP Secret by CryptoUtil
        	
        	//TODO Add new user to Database
        	
        	password = null;
        	repassword = null;
            //Prevent Session fixation, invalidate and assign a new session
        	session.invalidate();
            session = request.getSession(true);
            session.setAttribute("userid2fa", userid);
            //Set the session id cookie with HttpOnly, secure and samesite flags
            String custsession = "JSESSIONID=" + session.getId() + ";Path=/;Secure;HttpOnly;SameSite=Strict";
            response.setHeader("Set-Cookie", custsession);
            
            //Dispatch request to confirm.jsp
                        
        }

    }
}
