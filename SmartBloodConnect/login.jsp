<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Login</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>
<%
    String flashMessage = (String) session.getAttribute("flashMessage");
    String flashType = (String) session.getAttribute("flashType");
    if (flashMessage != null) {
        session.removeAttribute("flashMessage");
        session.removeAttribute("flashType");
    }
%>

<main class="page-shell">
    <div class="container small-container">
        <section class="card form-card">
            <div class="section-title left compact">
                <span class="eyebrow">Secure access</span>
                <h1>Login cleanly, continue quickly</h1>
            </div>

            <% if (flashMessage != null) { %>
                <div class="message <%= "error".equals(flashType) ? "error" : "success" %>"><%= flashMessage %></div>
            <% } %>
            <% if (flashMessage == null && request.getParameter("registered") != null) { %>
                <div class="message">Registration successful. Please login to continue.</div>
            <% } %>
            <% if (flashMessage == null && request.getParameter("error") != null) { %>
                <div class="message error"><%= "admin".equals(request.getParameter("error")) ? "Admin access is required." : "Login failed. Please check your credentials." %></div>
            <% } %>

            <form action="login" method="post">
                <div class="form-group">
                    <label for="loginType">Login Type</label>
                    <select id="loginType" name="loginType">
                        <option value="user">User</option>
                        <option value="admin">Admin</option>
                    </select>
                </div>

                <div class="form-group">
                    <label for="email">Email / Admin Username</label>
                    <input id="email" type="text" name="email" required>
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input id="password" type="password" name="password" required>
                </div>

                <button type="submit" class="btn btn-primary full-width">Login</button>
            </form>
        </section>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
