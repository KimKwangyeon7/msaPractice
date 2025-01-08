<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Word Count Rankings</title>
    <style>
        table {
            width: 50%;
            margin: auto;
            border-collapse: collapse;
        }
        th, td {
            border: 1px solid #ddd;
            padding: 8px;
            text-align: center;
        }
        th {
            background-color: #f4f4f4;
        }
        tr:nth-child(even) {
            background-color: #f9f9f9;
        }
        tr:hover {
            background-color: #f1f1f1;
        }
    </style>
</head>
<body>
<h1 style="text-align: center;">Word Count Rankings</h1>
<table>
    <thead>
    <tr>
        <th>Word</th>
        <th>Count</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach var="wordCount" items="${wordCounts}">
        <tr>
            <td>${wordCount.word()}</td>
            <td>${wordCount.count()}</td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
