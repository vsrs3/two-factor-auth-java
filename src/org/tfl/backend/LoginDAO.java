
package org.tfl.backend;

import java.util.logging.Logger;
import javax.servlet.ServletException;



public class LoginDAO
{

    private static final Logger log = Logger.getLogger(LoginDAO.class.getName());

    /**
     * Validates user credential
     * 
     * @param userid 
     * @param password
     * @param remoteip client ip address
     * @return true if user is valid, false otherwise
     */
    public static boolean validateUser(String userid, String password, String remoteip)
    {

    	boolean isValid = false;
    	/*TODO Pseudo code
    	 * result = SqlQuery(userid);
    	 * if result != 0
    	 * then
    	 * 		if account not lock
    	 * 		then
    	 * 			if hash(password+salt) is valid
    	 * 			then
    	 * 				isValid = true
    	 * 			else
    	 * 				begin
    	 * 					failLogin = failLogin + 1
    	 * 					if failLogin > MAX
    	 * 						Lock account
    	 * 				
    	 */
    	
    	return isValid;  
    }

    /**
     * Check if a user account is locked
     * 
     * @param userid
     * @param remoteip client ip address
     * @return true if account is locked or false otherwise
     * @throws ServletException
     */
    public static boolean isAccountLocked(String userid, String remoteip) throws ServletException
    {
    	boolean isLocked = false;
    	/*TODO Pseudo code
    	 * result = SqlQuery(userid);
    	 * if result != 0
    	 * then
    	 * 		if account is lock
    	 * 		then
    	 * 			isLocked = true
    	 * 				
    	 */
    	return isLocked;
    }
    
    /**
     * Increments the failed login count for a user
     * Locked the user account if fail logins exceed threshold.
     * 
     * @param userid
     * @param remoteip 
     * @throws ServletException
     */
    public static void incrementFailLogin(String userid, String remoteip)
            throws ServletException
    {
        if (userid == null || remoteip == null)
        {
        	//TODO throw exception;
        }
        
        //TODO Pseudo code
        /*
         *  failLogin = failLogin + 1
    	 * 	if failLogin > MAX
    	 * 		Lock account
    	 * 		update database
         */

    }
}
