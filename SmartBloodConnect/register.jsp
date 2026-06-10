<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Register</title>
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
    <div class="container">
        <div class="form-layout">
            <section class="form-intro">
                <span class="eyebrow">Simple registration</span>
                <h1>Create a donor or receiver account</h1>
                <p>Clean form design, minimal colors, and fast onboarding for new users.</p>
            </section>

            <section class="card form-card">
                <% if (flashMessage != null) { %>
                    <div class="message <%= "error".equals(flashType) ? "error" : "success" %>"><%= flashMessage %></div>
                <% } %>
                <% if (flashMessage == null && "exists".equals(request.getParameter("error"))) { %>
                    <div class="message error">This email is already registered.</div>
                <% } else if (flashMessage == null && request.getParameter("error") != null) { %>
                    <div class="message error">Registration failed. Please check your database setup.</div>
                <% } %>

                <form action="register" method="post">
                    <div class="form-group">
                        <label for="name">Full Name</label>
                        <input id="name" type="text" name="name" required>
                    </div>

                    <div class="grid-2">
                        <div class="form-group">
                            <label for="email">Email</label>
                            <input id="email" type="email" name="email" required>
                        </div>
                        <div class="form-group">
                            <label for="password">Password</label>
                            <input id="password" type="password" name="password" required>
                        </div>
                    </div>

                    <div class="grid-2">
                        <div class="form-group">
                            <label for="phone">Phone</label>
                            <input id="phone" type="text" name="phone" required>
                        </div>
                        <div class="form-group">
                            <label for="accountType">Account Type</label>
                            <select id="accountType" name="accountType" required>
                                <option value="DONOR">Donor</option>
                                <option value="RECEIVER">Receiver</option>
                            </select>
                        </div>
                    </div>

                    <div class="grid-2">
                        <div class="form-group">
                            <label for="city">City</label>
                            <input id="city" type="text" name="city" required>
                        </div>
                        <div class="form-group">
                            <label for="location">Location / Area</label>
                            <input id="location" type="text" name="location" required>
                        </div>
                    </div>

                    <div class="grid-2">
                        <div class="form-group">
                            <label for="bloodGroup">Blood Group</label>
                            <select id="bloodGroup" name="bloodGroup">
                                <option value="">Select blood group</option>
                                <option>A+</option><option>A-</option><option>B+</option><option>B-</option>
                                <option>AB+</option><option>AB-</option><option>O+</option><option>O-</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="lastDonationDate">Last Donation Date</label>
                            <input id="lastDonationDate" type="date" name="lastDonationDate">
                        </div>
                    </div>

                    <button type="submit" class="btn btn-primary full-width">Register Account</button>
                </form>
            </section>
        </div>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
