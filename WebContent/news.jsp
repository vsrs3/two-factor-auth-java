<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.tfl.backend.AuthSession" %>
<%@ page import="org.tfl.backend.AppConstants" %>
<%@ page import="org.tfl.backend.HtmlEscape" %>
<%@ page import="org.tfl.backend.NewsDAO" %>
<%@ page import="org.tfl.backend.NewsDAO.NewsItem" %>
<%@ page import="org.tfl.backend.UserDAO" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%
    response.setHeader("Cache-Control", "no-store");

// Verify user is authenticated
    if(!AuthSession.validate(request, response)) {
        return;
    }

// Get current user
    String userid = (String)session.getAttribute(AppConstants.SESSION_USERID);

// Get security level information
    int userSecurityLevel = (Integer)request.getAttribute("userSecurityLevel");
    String securityLevelName = (String)request.getAttribute("securityLevelName");

// Get news list
    List<NewsItem> newsList = (List<NewsItem>)request.getAttribute("newsList");

// Get messages from request attributes
    String postError = (String)request.getAttribute("postError");
    String postSuccess = (String)request.getAttribute("postSuccess");

// Format for displaying dates
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
%>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" type="text/css" href="styles/main.css">
    <title>News Board</title>
    <style>
        .news-item {
            border: 1px solid #ccc;
            margin: 10px 0;
            padding: 10px;
            background-color: #f9f9f9;
        }
        .news-header {
            display: flex;
            justify-content: space-between;
            border-bottom: 1px solid #ddd;
            padding-bottom: 5px;
            margin-bottom: 10px;
        }
        .news-content {
            margin: 10px 0;
            white-space: pre-wrap;
        }
        .security-label {
            font-weight: bold;
            padding: 2px 5px;
            border-radius: 3px;
        }
        .security-unclassified { background-color: #ddffdd; color: #006600; }
        .security-confidential { background-color: #ffffcc; color: #996600; }
        .security-secret { background-color: #ffdddd; color: #990000; }
        .security-topsecret { background-color: #ffaaaa; color: #660000; }
    </style>
</head>
<body>

<div class="mainbody">
    <h2>News Board</h2>

    <div style="margin-bottom: 20px;">
        Your security clearance: <span class="security-label
        <% if (userSecurityLevel == AppConstants.SECURITY_UNCLASSIFIED) { %>security-unclassified<% } %>
        <% if (userSecurityLevel == AppConstants.SECURITY_CONFIDENTIAL) { %>security-confidential<% } %>
        <% if (userSecurityLevel == AppConstants.SECURITY_SECRET) { %>security-secret<% } %>
        <% if (userSecurityLevel == AppConstants.SECURITY_TOP_SECRET) { %>security-topsecret<% } %>
        ">
            <%= securityLevelName %>
        </span>
    </div>

    <h3>Post News</h3>

    <% if (postError != null) { %>
    <div class="settingmsg"><%= HtmlEscape.escapeHTML(postError) %></div>
    <% } %>

    <% if (postSuccess != null) { %>
    <div style="color: green; font-weight: bold;"><%= HtmlEscape.escapeHTML(postSuccess) %></div>
    <% } %>

    <form method="POST" autocomplete="off" accept-charset="utf-8" action="/news">
        <ul class="form-ul">
            <li>
                <label>Content:</label>
                <textarea name="content" rows="5" cols="50" required></textarea>
            </li>
            <li>
                <label>Security Level:</label>
                <select name="securitylevel">
                    <% if (userSecurityLevel <= AppConstants.SECURITY_UNCLASSIFIED) { %>
                    <option value="<%= AppConstants.SECURITY_UNCLASSIFIED %>">
                        <%= NewsDAO.getSecurityLabelName(AppConstants.SECURITY_UNCLASSIFIED) %>
                    </option>
                    <% } %>
                    <% if (userSecurityLevel <= AppConstants.SECURITY_CONFIDENTIAL) { %>
                    <option value="<%= AppConstants.SECURITY_CONFIDENTIAL %>">
                        <%= NewsDAO.getSecurityLabelName(AppConstants.SECURITY_CONFIDENTIAL) %>
                    </option>
                    <% } %>
                    <% if (userSecurityLevel <= AppConstants.SECURITY_SECRET) { %>
                    <option value="<%= AppConstants.SECURITY_SECRET %>">
                        <%= NewsDAO.getSecurityLabelName(AppConstants.SECURITY_SECRET) %>
                    </option>
                    <% } %>
                    <% if (userSecurityLevel <= AppConstants.SECURITY_TOP_SECRET) { %>
                    <option value="<%= AppConstants.SECURITY_TOP_SECRET %>">
                        <%= NewsDAO.getSecurityLabelName(AppConstants.SECURITY_TOP_SECRET) %>
                    </option>
                    <% } %>
                </select>
            </li>
            <li>
                <input type="submit" value="Post News">
            </li>
        </ul>
    </form>

    <h3>News Feed</h3>

    <% if (newsList == null || newsList.isEmpty()) { %>
    <p>No news items available.</p>
    <% } else { %>
    <% for (NewsItem newsItem : newsList) { %>
    <div class="news-item">
        <div class="news-header">
            <div>
                <strong>Posted by:</strong> <%= HtmlEscape.escapeHTML(newsItem.getUserid()) %>
                <span style="margin-left: 10px;">
                            <strong>Date:</strong> <%= dateFormat.format(newsItem.getDate()) %>
                        </span>
            </div>
            <div>
                        <span class="security-label
                        <% if (newsItem.getSecurityLabel() == AppConstants.SECURITY_UNCLASSIFIED) { %>security-unclassified<% } %>
                        <% if (newsItem.getSecurityLabel() == AppConstants.SECURITY_CONFIDENTIAL) { %>security-confidential<% } %>
                        <% if (newsItem.getSecurityLabel() == AppConstants.SECURITY_SECRET) { %>security-secret<% } %>
                        <% if (newsItem.getSecurityLabel() == AppConstants.SECURITY_TOP_SECRET) { %>security-topsecret<% } %>
                        ">
                            <%= NewsDAO.getSecurityLabelName(newsItem.getSecurityLabel()) %>
                        </span>
            </div>
        </div>
        <div class="news-content">
            <%= HtmlEscape.escapeHTML(newsItem.getContent()) %>
        </div>
    </div>
    <% } %>
    <% } %>

    <div style="margin-top: 20px;">
        <a href="/success.jsp">Back to Welcome Page</a> |
        <% if ("admin".equals(userid)) { %>
        <a href="/securitylabel">Manage Security Labels</a> |
        <% } %>
        <a href="/logout.jsp">Logout</a>
    </div>
</div>

<%@include file="templates/footer.html" %>

</body>
</html>