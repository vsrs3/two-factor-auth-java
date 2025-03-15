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


<form method="POST" autocomplete="off" accept-charset="utf-8" action="/register">
<ul class="form-ul" >

<li>
<label>First name</label>
<input type="text" required autofocus name="firstname">
</li>

<li>
<label>Last name</label>
<input type="text" required name="lastname">
</li>

<li>
<label>Username</label>
<input type="text" required name="userid">
</li>

<li>
<label>Password</label>
<input type="password" required name="password">
</li>

<li>
<label>Retype password</label>
<input type="password" required name="repassword">
</li>

<li>
<input type="submit" value="Register" >
</li>
</ul>
</form>

</div>

<%@include file="templates/footer.html" %>

</body>
</html>