
package org.tfl.crypto;

import java.util.Date;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class TimeBaseOTP
{

    private static final Logger log = Logger.getLogger(TimeBaseOTP.class.getName());

    /**
     * Generates a TOTP that can be used with google authenticator based on the
     * rfc6238 https://tools.ietf.org/html/rfc6238
     * 
     */
    public static String generateOTP(byte[] secretkey)
    {
        // Get current time as counter
        Date curdate = new Date();
        long currenttime = curdate.getTime();
        long authtime = (currenttime / 1000) / 30;
        String hextime = Long.toHexString(authtime);

        // Pad hextime with leading 0 so that hextime becomes a 16
        // character hex string
        int hexlength = hextime.length();
        if (hexlength < 16)
        {
            StringBuilder buf = new StringBuilder(32);
            int padlength = 16 - hexlength;

            for (int i = 0; i < padlength; i++)
            {
                buf.append("0");
            }

            buf.append(hextime);
            hextime = buf.toString();

        }

        byte timecounter[] = CryptoUtil.hexStringToByteArray(hextime);

        try
        {
            SecretKeySpec key = new SecretKeySpec(secretkey, "HmacSHA1");
            Mac hmac_sha1 = Mac.getInstance("HmacSHA1");
            hmac_sha1.init(key);

            byte[] ret = hmac_sha1.doFinal(timecounter);

            // Last 4 bits of the hmac is the index into the next 4 bytes to be
            // returned as int digit
            byte lastbyte = ret[ret.length -1];
            int index = lastbyte & 0x0f;
           
            int otpvalue = 0; 
            int octet = 0;
            int shift = 24; 
            
            for(int i=0;i<4;i++)
            {
                
                int mask = 0;
                if(i==0)
                {
                   // & with 0x7f so that the most significant byte
                   // is unsigned
                    mask = 0x7f;
                }
                else
                {
                    mask = 0xff;
                }
                
                octet = 0;
                octet = (octet | (ret[index + i] & mask)) << shift;
                shift = shift - 8; 
                otpvalue =  otpvalue | octet;
                
            }
            
            // To get 6 digit otp ,get the reminder from modulus 1000000
            otpvalue = otpvalue % 1000000;

            String otpresult = Integer.toString(otpvalue);

            // Pad otpresult with leading 0 if it is less than 6 digit
            int otpresultlength = otpresult.length();

            if (otpresultlength < 6)
            {
                StringBuilder buf = new StringBuilder(32);
                int padlength = 6 - otpresultlength;

                for (int i = 0; i < padlength; i++)
                {
                    buf.append("0");
                }

                buf.append(otpresult);
                otpresult = buf.toString();

            }

            return otpresult;

        }
        catch (NoSuchAlgorithmException e)
        {
            log.warning("Error: TimeBaseOTP " + e);
            return null;
        }
        catch (InvalidKeyException e)
        {
            log.warning("Error: TimeBaseOTP " + e);
            return null;
        }

    }

}
