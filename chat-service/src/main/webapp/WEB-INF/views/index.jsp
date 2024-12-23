<%@ page import="java.util.List" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <title>채팅 서비스 메인 페이지</title>
</head>
<body>
<h1>채팅 서비스</h1>
<ul>
    <%
        List<String> chatRooms = (List<String>) request.getAttribute("chatRooms");
        for (int i = 0; i < chatRooms.size(); i++) {
    %>
    <li>
        <a href="room/<%= i + 1 %>">
            <%= chatRooms.get(i) %>
        </a>
    </li>
    <% } %>
</ul>
</body>
</html>
