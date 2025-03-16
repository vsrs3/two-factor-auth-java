
package org.tfl.backend;

public class AppConstants {

    // Minimum length for passwords (required per project spec)
    public static final int MIN_LENGTH_PASS = 6;

    // Maximum number of failed login attempts before account is locked
    public static final int MAX_FAIL_LOGIN = 5;

    // Number of iterations for Password Based Key Derivation 2
    public static final int PBE_ITERATION = 10000;

    // Security label constants
    public static final int SECURITY_UNCLASSIFIED = 1;
    public static final int SECURITY_CONFIDENTIAL = 2;
    public static final int SECURITY_SECRET = 3;
    public static final int SECURITY_TOP_SECRET = 4;

    // Session attribute names
    public static final String SESSION_USERID = "userid";
    public static final String SESSION_USERID_2FA = "userid2fa";
    public static final String SESSION_OTP_ERROR = "otperror";
    public static final String SESSION_ANTICSRF = "anticsrf_success";
}