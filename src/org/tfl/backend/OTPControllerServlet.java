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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");
       
        
        // Make sure it has a valid 2fa session from login page
        // userid2fa session attribute must be set

        //TODO check2FASession
        
        HttpSession session = request.getSession(false);
        String userid = (String) session.getAttribute("userid2fa");
        // Remove the userid2fa attribute to prevent multiple submission attempts
        session.removeAttribute("userid2fa");

        String otpvalue = (String) request.getParameter("totp");

        if (otpvalue == null)
        {
        	//TODO Invalidate session
        	
        	//TODO Redirect error.html
        }
        
        String otpsecret = null;
        //TODO Get otpsecret from Database using OTPDAO class 
        
        //GenerateOTP using TimeBaseOTP class
        String otpresult = TimeBaseOTP.generateOTP(CryptoUtil.hexStringToByteArray(otpsecret));
        
        otpsecret = null;

        if (otpresult == null)
        {
        	//TODO Invalidate session
        	
        	//TODO Redirect error.html 
        }

        if (otpresult.equals(otpvalue))
        {// Correct OTP value
        	
        	//TODO Redirect error.html 
            session.invalidate();
            session = request.getSession(true);
            session.setAttribute("userid", userid);
            session.setAttribute("anticsrf_success", "AntiCSRF");

            String custsession = "JSESSIONID=" + session.getId() + ";Path=/;Secure;HttpOnly;SameSite=Strict";
            response.setHeader("Set-Cookie", custsession);
            
            //TODO reset fail login attempt
           
            //TODO Redirect /success.jsp
            
        }
        else
        {// Incorrect OTP value
            
            String remoteip = request.getRemoteAddr();
            log.warning("Error: Invalid otp value " + request.getRemoteAddr() + " " + userid);
            
            // TODO Update fail login count.
           
            
            //If account is locked reset session and redirect user
            //TODO pseudo code
            /*
             * 	if account is locked
             * 	then
             * 		redirect /locked.html
             *  else
             *  	session.setAttribute("userid2fa", userid);
             *   	session.setAttribute("otperror", "");
             *  	send back to the otp input page again
             * 
             */
           

        }

    }

}
