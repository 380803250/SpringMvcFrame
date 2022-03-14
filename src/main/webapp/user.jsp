<%@ page contentType="text/html;charset=UTF-8" language="java" isELIgnored="false" %>
<html>
<head>
    <title>User.jsp</title>
</head>
<body>
    <%--不忽略EL表达式并且取值--%>
    <h1>${requestScope.userMessage}</h1>
</body>
</html>
