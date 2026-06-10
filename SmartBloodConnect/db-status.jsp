<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Database Status</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>
<%
    Boolean dbConnected = (Boolean) request.getAttribute("dbConnected");
    List<String> tables = (List<String>) request.getAttribute("tables");
%>

<main class="page-shell">
    <div class="container small-container">
        <section class="card form-card">
            <div class="section-title left compact">
                <span>JDBC and MySQL</span>
                <h1>Database Connection Status</h1>
            </div>

            <% if (Boolean.TRUE.equals(dbConnected)) { %>
                <div class="message success"><%= request.getAttribute("dbMessage") %></div>
            <% } else { %>
                <div class="message error"><%= request.getAttribute("dbMessage") %></div>
            <% } %>

            <div class="status-block">
                <p><strong>JDBC URL:</strong> <%= request.getAttribute("dbUrl") %></p>
                <p><strong>DB User:</strong> <%= request.getAttribute("dbUser") %></p>
                <p><strong>Database:</strong> <%= request.getAttribute("dbProduct") == null ? "Not connected" : request.getAttribute("dbProduct") %></p>
                <p><strong>Version:</strong> <%= request.getAttribute("dbVersion") == null ? "Not connected" : request.getAttribute("dbVersion") %></p>
            </div>

            <h3 style="margin-top:1.25rem;">Detected Tables</h3>
            <div class="card inner-card">
                <% if (tables != null && !tables.isEmpty()) { %>
                    <% for (String table : tables) { %>
                        <p><%= table %></p>
                    <% } %>
                <% } else { %>
                    <p>No tables found yet. Run `database.sql` in MySQL.</p>
                <% } %>
            </div>

            <div class="hero-actions" style="margin-top:1.25rem;">
                <a href="db-status" class="btn btn-primary">Refresh Status</a>
                <a href="index.jsp" class="btn btn-outline">Back Home</a>
            </div>
        </section>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
