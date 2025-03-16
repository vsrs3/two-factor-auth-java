<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.tfl.backend.AuthSession" %>
<%@ page import="org.tfl.backend.AppConstants" %>
<%@ page import="org.tfl.backend.HtmlEscape" %>
<%@ page import="org.tfl.backend.NewsDAO" %>

<%
    response.setHeader("Cache-Control", "no-store");

// Verify user is authenticated
    if(!AuthSession.validate(request, response)) {
        return;
    }

// Check if user is admin
    String userid = (String)session.getAttribute(AppConstants.SESSION_USERID);
    if (!"admin".equals(userid)) {
        response.sendRedirect("/error.html");
        return;
    }

// Get messages from request attributes
    String errorMsg = (String)request.getAttribute("errorMsg");
    String successMsg = (String)request.getAttribute("successMsg");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" type="text/css" href="styles/main.css">
    <title>Security Label Assignment</title>
</head>
<body>

<div class="mainbody">
    <h2>Security Label Assignment (Admin Only)</h2>

    <% if (errorMsg != null) { %>
    <div class="settingmsg"><%= HtmlEscape.escapeHTML(errorMsg) %></div>
    <% } %>

    <% if (successMsg != null) { %>
    <div style="color: green; font-weight: bold;"><%= HtmlEscape.escapeHTML(successMsg) %></div>
    <% } %>

    <form method="POST" autocomplete="off" accept-charset="utf-8" action="/securitylabel">
        <ul class="form-ul">
            <li>
                <label>Target User ID:</label>
                <input type="text" required name="targetuser" size="25">
            </li>
            <li>
                <label>Security Level:</label>
                <select name="securitylevel">
                    <option value="<%= AppConstants.SECURITY_UNCLASSIFIED %>">
                        <%= NewsDAO.getSecurityLabelName(AppConstants.SECURITY_UNCLASSIFIED) %>
                    </option>
                    <option value="<%= AppConstants.SECURITY_CONFIDENTIAL %>">
                        <%= NewsDAO.getSecurityLabelName(AppConstants.SECURITY_CONFIDENTIAL) %>
                    </option>
                    <option value="<%= AppConstants.SECURITY_SECRET %>">
                        <%= NewsDAO.getSecurityLabelName(AppConstants.SECURITY_SECRET) %>
                    </option>
                    <option value="<%= AppConstants.SECURITY_TOP_SECRET %>">
                        <%= NewsDAO.getSecurityLabelName(AppConstants.SECURITY_TOP_SECRET) %>
                    </option>
                </select>
            </li>
            <li>
                <input type="submit" value="Apply">
            </li>
        </ul>
    </form>

    <div style="margin-top: 20px;">
        <a href="/success.jsp">Back to Welcome Page</a> |
        <a href="/news">View News</a> |
        <a href="/logout.jsp">Logout</a>
    </div>
</div>

<%@include file="templates/footer.html" %>

</body>
</html>
