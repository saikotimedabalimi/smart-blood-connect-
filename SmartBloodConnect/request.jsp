<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.smartblood.model.User" %>
<%
    String flashMessage = (String) session.getAttribute("flashMessage");
    String flashType = (String) session.getAttribute("flashType");
    if (flashMessage != null) {
        session.removeAttribute("flashMessage");
        session.removeAttribute("flashType");
    }

    User currentUser = (User) session.getAttribute("user");
    String donorUserId = request.getParameter("donorUserId") == null ? "" : request.getParameter("donorUserId");
    String donorName = request.getParameter("donorName") == null ? "" : request.getParameter("donorName");
    String bloodGroupValue = request.getParameter("bloodGroup") == null ? "" : request.getParameter("bloodGroup");
    String cityValue = request.getParameter("city") == null ? "" : request.getParameter("city");
    String locationValue = request.getParameter("location") == null ? "" : request.getParameter("location");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Raise Blood Request</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="page-shell">
    <div class="container">
        <div class="form-layout">
            <section class="form-intro">
                <span class="eyebrow">Request routing</span>
                <h1>Raise a request for a direct donor or broadcast it to matching donors</h1>
                <p>Donors can accept or reject, then you can share procurer contact details and hospital formalities from your dashboard.</p>
            </section>

            <section class="card form-card">
                <% if (flashMessage != null) { %>
                    <div class="message <%= "error".equals(flashType) ? "error" : "success" %>"><%= flashMessage %></div>
                <% } %>
                <% if (flashMessage == null && request.getParameter("error") != null) { %>
                    <div class="message error">Request could not be submitted. Please review the selected donor and login status.</div>
                <% } %>

                <% if (!donorUserId.isEmpty()) { %>
                    <div class="selected-donor-banner">
                        <span class="eyebrow alt">Direct donor request</span>
                        <h3><%= donorName.isEmpty() ? "Selected donor" : donorName %></h3>
                        <p>This request will go straight to the selected donor first.</p>
                    </div>
                <% } else { %>
                    <div class="selected-donor-banner subtle">
                        <span class="eyebrow alt">General broadcast request</span>
                        <p>Matching available donors in the selected city will receive notifications.</p>
                    </div>
                <% } %>

                <% if (currentUser == null) { %>
                    <div class="message error">Login is required before you can submit a blood request.</div>
                <% } %>

                <form action="request" method="post">
                    <input type="hidden" name="donorUserId" value="<%= donorUserId %>">
                    <div class="form-group">
                        <label for="patientName">Patient Name</label>
                        <input id="patientName" type="text" name="patientName" required>
                    </div>

                    <div class="grid-2">
                        <div class="form-group">
                            <label for="bloodGroup">Required Blood Group</label>
                            <select id="bloodGroup" name="bloodGroup" required>
                                <option value="">Select blood group</option>
                                <option value="A+" <%= "A+".equals(bloodGroupValue) ? "selected" : "" %>>A+</option>
                                <option value="A-" <%= "A-".equals(bloodGroupValue) ? "selected" : "" %>>A-</option>
                                <option value="B+" <%= "B+".equals(bloodGroupValue) ? "selected" : "" %>>B+</option>
                                <option value="B-" <%= "B-".equals(bloodGroupValue) ? "selected" : "" %>>B-</option>
                                <option value="AB+" <%= "AB+".equals(bloodGroupValue) ? "selected" : "" %>>AB+</option>
                                <option value="AB-" <%= "AB-".equals(bloodGroupValue) ? "selected" : "" %>>AB-</option>
                                <option value="O+" <%= "O+".equals(bloodGroupValue) ? "selected" : "" %>>O+</option>
                                <option value="O-" <%= "O-".equals(bloodGroupValue) ? "selected" : "" %>>O-</option>
                            </select>
                        </div>
                        <div class="form-group">
                            <label for="unitsRequired">Units Required</label>
                            <input id="unitsRequired" type="number" name="unitsRequired" min="1" value="1" required>
                        </div>
                    </div>

                    <div class="grid-2">
                        <div class="form-group">
                            <label for="city">City</label>
                            <input id="city" type="text" name="city" value="<%= cityValue %>" required>
                        </div>
                        <div class="form-group">
                            <label for="location">Hospital / Area</label>
                            <input id="location" type="text" name="location" value="<%= locationValue %>" required>
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="reason">Reason / Patient Condition</label>
                        <textarea id="reason" name="reason" rows="5" required></textarea>
                    </div>

                    <div class="checkbox-row">
                        <input id="emergency" type="checkbox" name="emergency">
                        <label for="emergency">Send as emergency blood request</label>
                    </div>

                    <button type="submit" class="btn btn-primary full-width" <%= currentUser == null ? "disabled" : "" %>>Submit Blood Request</button>
                </form>
            </section>
        </div>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
