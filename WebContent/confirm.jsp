<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@page import="org.tfl.backend.AuthSession" %>
<%@page import="org.tfl.backend.OTPDAO" %>
<%@page import="org.tfl.backend.HtmlEscape" %>
<%@page import="org.tfl.crypto.CryptoUtil" %>

<%
    response.setHeader("Cache-Control", "no-store");

// Verify user has completed first authentication step
    if(!AuthSession.check2FASession(request, response, "/index.jsp"))
    {
        return;
    }

// Get the userid from session
    String userid = (String)session.getAttribute("userid2fa");

// Get OTP secret for QR code generation
    String base32OtpSecret = null;
    try {
        base32OtpSecret = OTPDAO.getBase32OTPSecret(userid, request.getRemoteAddr());
    } catch (Exception e) {
        // Log the error and redirect to error page
        request.getSession().invalidate();
        response.sendRedirect("/error.html");
        return;
    }

    if (base32OtpSecret == null) {
        // OTP secret not found, redirect to error page
        request.getSession().invalidate();
        response.sendRedirect("/error.html");
        return;
    }

// For security, HTML escape the OTP secret
    base32OtpSecret = HtmlEscape.escapeHTML(base32OtpSecret);
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" type="text/css" href="styles/main.css">
    <title>2 Factor Authentication Setup</title>
</head>
<body>

<div class="webformdiv">

    <div class="formheader">
        <h3>Device Confirmation</h3>
    </div>

    <div style="margin: 10px 0px;">
        <p>Please scan this code with Google Authenticator:</p>
        <p>Or enter this key manually: <strong><%= base32OtpSecret %></strong></p>
        <p>Once set up, enter the verification code below:</p>
    </div>

    <form method="POST" autocomplete="off" accept-charset="utf-8" action="/otpctl">
        <ul class="form-ul" >

            <li>
                <label>Enter OTP</label>
            </li>

            <li>
                <div id="msg" class="settingmsg">

                    <%
                        // Check for OTP error message
                        String otperror = (String) session.getAttribute("otperror");
                        if (otperror != null)
                        {
                            session.removeAttribute("otperror");
                            out.println("Invalid OTP");
                        }
                    %>

                </div>
                <input type="text" required name="totp" size="25" >
            </li>
            <li>
                <input type="submit" value="Confirm" >
            </li>

        </ul>
    </form>

</div>

<%@include file="templates/footer.html" %>

</body>
</html>