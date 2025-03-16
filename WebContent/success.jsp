<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<%@page import="org.tfl.backend.UserDAO" %>
<%@page import="org.tfl.backend.AuthSession" %>
<%@page import="org.tfl.backend.HtmlEscape" %>
<%@page import="org.tfl.backend.AppConstants" %>
<%@page import="org.tfl.backend.NewsDAO" %>

<%
    response.setHeader("Cache-Control", "no-store");
    if(!AuthSession.validate(request, response))
    {
        return;
    }

    String userid = (String)session.getAttribute(AppConstants.SESSION_USERID);

    if(userid == null)
    {
        response.sendRedirect("/error.html");
        return;
    }

// Prevent CSRF by requiring OTP validation each time page is displayed.
    String anticsrf = (String)session.getAttribute(AppConstants.SESSION_ANTICSRF);
    if(anticsrf == null)
    {
        // Token not present, redirect back to OTP page for validation again
        session.removeAttribute(AppConstants.SESSION_USERID);
        session.setAttribute(AppConstants.SESSION_USERID_2FA, userid);
        userid = null;
        RequestDispatcher rd = request.getRequestDispatcher("otp.jsp");
        rd.forward(request, response);
        return;
    }
    else
    {
        // Token present
        // Remove the token so that subsequent request will require OTP validation
        session.removeAttribute(AppConstants.SESSION_ANTICSRF);
    }

    String username = UserDAO.getUserName(userid, request.getRemoteAddr());
    username = HtmlEscape.escapeHTML(username);

// Get user's security level
    int userSecurityLevel = UserDAO.getUserSecurityLevel(userid, request.getRemoteAddr());
    String securityLevelName = NewsDAO.getSecurityLabelName(userSecurityLevel);
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" type="text/css" href="styles/main.css">
    <title>Welcome</title>
    <style>
        .nav-menu {
            margin: 20px 0;
        }
        .nav-menu a {
            display: inline-block;
            padding: 8px 15px;
            margin: 5px;
            background-color: #0054b8;
            color: white;
            text-decoration: none;
            border-radius: 5px;
        }
        .nav-menu a:hover {
            background-color: #003a80;
        }
        .security-info {
            margin: 15px 0;
            padding: 10px;
            background-color: #f0f0f0;
            border-radius: 5px;
            border-left: 5px solid #0054b8;
        }
    </style>
</head>
<body>

<div class="mainbody">

    <h2>Welcome to Secure Application</h2>

    <div class="security-info">
        <p>
            <strong>User:</strong> <%= username != null ? username : "Unknown" %> (<%= userid %>)<br>
            <strong>Security Clearance:</strong> <%= securityLevelName %><br>
        </p>
    </div>

    <div class="nav-menu">
        <a href="/news">View & Post News</a>

        <% if ("admin".equals(userid)) { %>
        <a href="/securitylabel">Manage Security Labels</a>
        <% } %>

        <a href="/logout.jsp">Logout</a>
    </div>

    <p>
        You have successfully authenticated using two-factor authentication.
    </p>

    <p>
        This application implements the Bell-Lapadula security model for access control:
    <ul>
        <li><strong>No-Read-Up:</strong> You can only read news items at or below your security level.</li>
        <li><strong>No-Write-Down:</strong> You can only post news items at or above your security level.</li>
    </ul>
    </p>

    <%@include file="templates/footer.html" %>

</div>

</body>
</html>