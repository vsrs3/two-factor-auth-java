package org.tfl.backend;

public class RegisterDAO {
	
	 /**
     * Check user existence
     * 
     * @param userid 
     * @param remoteip client ip address
     * @return true if user is existent, false otherwise
     */
	public static boolean findUser(String userid, String remoteip)
	{
		 boolean isFound = false;
		 //TODO Pseudo code
		 /*
		  *  result = SqlQuery(userid);
		  *  if result != 0
		  *  then
		  *  	isFound = true
		  */
		 
		 return isFound;
	}
	
	public static boolean addUser(String firstName, String lastName, String userid, String password, String salt, String otpSecret, String remoteip)
	{
		boolean isSuccess = false;
		
		//TODO Hash password with salt
		
		//TODO Insert new record to database. If operation is success set isSuccess to true
		
		return isSuccess;
	}
		
}
