<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="java.util.logging.Logger" %>    
    
<%!  private static final Logger log = Logger.getLogger("logout.jsp"); %>    
    
<%

   response.setHeader("Cache-Control", "no-store");

   if(session.getAttribute("userid") != null || session.getAttribute("userid2fa") != null)
   {
        session.invalidate();
        String custsession = "JSESSIONID=" + " " +";Path=/;Secure;HttpOnly;SameSite=Strict";
        response.setHeader("Set-Cookie", custsession);
   }
   else
   {
       log.warning("Error: Invalid logout attempt " + request.getRemoteAddr());
       session.invalidate(); 
       String custsession = "JSESSIONID=" + " " +";Path=/;Secure;HttpOnly;SameSite=Strict";
       response.setHeader("Set-Cookie", custsession);
       response.sendRedirect("/error.html");
       return;
   }

%>    
    
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="styles/main.css">
<title>Logout Page</title>
</head>
<body>

<div class="mainbody">

<p>
You have been logged out !
</p>

<p>
<a href="/index.jsp">Login Again</a>
</p>

<%@include file="templates/footer.html" %>


</div>



</body>
</html>