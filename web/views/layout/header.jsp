<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%
    String title = request.getParameter("title");
    if (title == null || title.isEmpty()) {
        title = "Driving License Portal";
    }
    Boolean noSidebar = (Boolean) request.getAttribute("noSidebar");
    if (noSidebar == null) {
        noSidebar = false;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= title %></title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/assets/css/style.css">
</head>
<body class="<%= noSidebar ? "no-sidebar" : "" %>">
    <div class="app-container">
