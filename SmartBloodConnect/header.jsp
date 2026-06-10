<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.smartblood.model.User" %>
<%
    User headerUser = (User) session.getAttribute("user");
    boolean adminLoggedIn = Boolean.TRUE.equals(session.getAttribute("adminLoggedIn"));
%>
<nav class="navbar">
    <div class="container navbar-inner">
        <a href="index.jsp" class="logo-wrap">
            <span class="logo-mark">SB</span>
            <span class="logo-text">Smart Blood Connect</span>
        </a>

        <div class="nav-links">
            <a href="index.jsp">Home</a>
            <a href="search">Donors</a>
            <a href="compatibility.jsp">Compatibility</a>
            <a href="request.jsp">Request</a>
            <% if (headerUser == null && !adminLoggedIn) { %>
                <a href="register.jsp">Register</a>
                <a href="login.jsp">Login</a>
            <% } else if (adminLoggedIn || (headerUser != null && "ADMIN".equalsIgnoreCase(headerUser.getRole()))) { %>
                <a href="admin">Admin</a>
                <a href="logout">Logout</a>
            <% } else { %>
                <a href="dashboard">Dashboard</a>
                <a href="logout">Logout</a>
            <% } %>
            <button type="button" id="theme-toggle" class="theme-toggle" aria-label="Toggle theme">Theme</button>
        </div>
    </div>
</nav>
