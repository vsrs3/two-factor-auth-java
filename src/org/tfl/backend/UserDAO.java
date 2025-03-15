package org.tfl.backend;

import java.util.logging.Logger;

public class UserDAO
{
    private static final Logger log = Logger.getLogger(UserDAO.class.getName());

    /**
     * Retrieves the username for a userid
     * 
     * @param userid
     * @param remoteip
     * @return username if successful, null if there is an error
     */
    public static String getUserName(String userid, String remoteip)
    {
        String fullName = null;
    	//TODO Query the full name of user forom database 
        
        return fullName;
    }
    
    
}
