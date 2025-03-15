
package org.tfl.backend;

import java.util.logging.Logger;
import javax.servlet.ServletException;

import org.tfl.crypto.CryptoUtil;

public class OTPDAO
{

    private static final Logger log = Logger.getLogger(OTPDAO.class.getName());

    /**
     * Retrieves the otp secret hexadecimal string from the userid
     * 
     * @param userid
     * @param remoteip
     * @return hexadecimal secret string 
     * @throws ServletException
     */
    public static String getOTPSecret(String userid, String remoteip) throws ServletException
    {
        String otpSecret = null;
    	if (userid == null || remoteip == null)
        {
            //TODO throw exception;
        }

        //TODO Query otp from database
    	
    	return otpSecret;
    }
    
    /**
     * Retrieves the otp secret hexadecimal string from the userid
     * 
     * @param userid
     * @param remoteip
     * @return hexadecimal secret string 
     * @throws ServletException
     */
    public static String getBase32OTPSecret(String userid, String remoteip) throws ServletException
    {
        String hexaString = getOTPSecret(userid, remoteip);
        byte[] hexaCode = CryptoUtil.hexStringToByteArray(hexaString);
        return CryptoUtil.base32Encode(hexaCode);
    }

    /**
     * Check if a user account is locked
     * 
     * @param userid
     * @param remoteip
     * @return true if account is locked, false otherwise
     * @throws ServletException
     */
    public static boolean isAccountLocked(String userid, String remoteip) throws ServletException
    {
        boolean isLock = true;
    	if (userid == null || remoteip == null)
        {
        	//TODO throw exception;
        }

        //TODO Query database to check account's status
    	return isLock;
    }

   
    /**
     * Reset the failed login counts of a user to zero 
     * If an account is locked an exception will be thrown
     * 
     * @param userid
     * @param remoteip
     * @throws ServletException
     */
    public static void resetFailLogin(String userid, String remoteip) throws ServletException
    {
        if (userid == null || remoteip == null)
        {
        	//TODO throw exception;
        }

        //TODO Update failLogin in database to zero
    }

}
