<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
response.setHeader("Cache-Control", "no-store"); 
%>
    
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<link rel="stylesheet" type="text/css" href="styles/main.css">
<title>Login Page</title>
</head>
<body>



<div class="webformdiv">

<div class="formheader">
<h3>Application Sign In</h3>
</div>


<form method="POST" autocomplete="off" accept-charset="utf-8" action="/login">
<ul class="form-ul" >
<li>
<label>Username</label>
<input type="text" required autofocus name="userid">
</li>
<li>
<label>Password</label>
<input type="password" required name="password">
</li>
<li>
<input type="submit" value="Login" >
</li>
</ul>
</form>

</div>

<%@include file="templates/footer.html" %>

</body>
</html>