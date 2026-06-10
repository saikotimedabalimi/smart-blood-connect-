<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, com.smartblood.dao.DonorDAO, com.smartblood.model.Donor, com.smartblood.model.User" %>
<%
    String selectedBloodGroup = request.getParameter("bloodGroup") == null ? "" : request.getParameter("bloodGroup");
    String selectedCity = request.getParameter("city") == null ? "" : request.getParameter("city");
    List<Donor> donors = (List<Donor>) request.getAttribute("donors");
    if (donors == null) {
        donors = new DonorDAO().searchDonors(selectedBloodGroup, selectedCity);
    }
    User currentUser = (User) session.getAttribute("user");
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Search Available Blood Donors</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="page-shell">
    <div class="container">
        <div class="section-title left">
            <span class="eyebrow">Blood availability search</span>
            <h1>Find compatible donors and raise a request directly</h1>
        </div>

        <section class="card search-panel">
            <form action="search" method="get" class="search-form">
                <div class="form-group">
                    <label for="bloodGroup">Patient Blood Group</label>
                    <select id="bloodGroup" name="bloodGroup">
                        <option value="">All compatible groups</option>
                        <option value="A+" <%= "A+".equals(selectedBloodGroup) ? "selected" : "" %>>A+</option>
                        <option value="A-" <%= "A-".equals(selectedBloodGroup) ? "selected" : "" %>>A-</option>
                        <option value="B+" <%= "B+".equals(selectedBloodGroup) ? "selected" : "" %>>B+</option>
                        <option value="B-" <%= "B-".equals(selectedBloodGroup) ? "selected" : "" %>>B-</option>
                        <option value="AB+" <%= "AB+".equals(selectedBloodGroup) ? "selected" : "" %>>AB+</option>
                        <option value="AB-" <%= "AB-".equals(selectedBloodGroup) ? "selected" : "" %>>AB-</option>
                        <option value="O+" <%= "O+".equals(selectedBloodGroup) ? "selected" : "" %>>O+</option>
                        <option value="O-" <%= "O-".equals(selectedBloodGroup) ? "selected" : "" %>>O-</option>
                    </select>
                </div>
                <div class="form-group">
                    <label for="city">City</label>
                    <input id="city" type="text" name="city" value="<%= selectedCity %>" placeholder="Enter city">
                </div>
                <div class="search-action">
                    <button type="submit" class="btn btn-primary">Search</button>
                </div>
            </form>
        </section>

        <section class="results-grid">
            <% if (donors != null && !donors.isEmpty()) {
                   for (Donor donor : donors) { %>
                <article class="card donor-card">
                    <div class="card-topline">
                        <div class="blood-badge"><%= donor.getBloodGroup() %></div>
                        <span class="status approved"><%= donor.isAvailable() ? "Available" : "Unavailable" %></span>
                    </div>
                    <h3><%= donor.getName() %></h3>
                    <p><strong>City:</strong> <%= donor.getCity() %></p>
                    <p><strong>Location:</strong> <%= donor.getLocation() %></p>
                    <p><strong>Phone:</strong> <%= donor.getPhone() %></p>
                    <p><strong>Email:</strong> <%= donor.getEmail() %></p>
                    <div class="hero-actions compact-actions">
                        <% if (currentUser != null) { %>
                            <a href="request.jsp?donorUserId=<%= donor.getUserId() %>&bloodGroup=<%= java.net.URLEncoder.encode(selectedBloodGroup == null || selectedBloodGroup.isEmpty() ? donor.getBloodGroup() : selectedBloodGroup, "UTF-8") %>&city=<%= java.net.URLEncoder.encode(selectedCity == null || selectedCity.isEmpty() ? donor.getCity() : selectedCity, "UTF-8") %>&location=<%= java.net.URLEncoder.encode(donor.getLocation(), "UTF-8") %>&donorName=<%= java.net.URLEncoder.encode(donor.getName(), "UTF-8") %>" class="btn btn-primary btn-small">Request This Donor</a>
                        <% } else { %>
                            <a href="login.jsp?error=login" class="btn btn-primary btn-small">Login to Request</a>
                        <% } %>
                        <a href="compatibility.jsp?group=<%= java.net.URLEncoder.encode(donor.getBloodGroup(), "UTF-8") %>" class="btn btn-outline btn-small">Check Match</a>
                    </div>
                </article>
            <%     }
               } else { %>
                <div class="card empty-state">
                    <h3>No available donors found</h3>
                    <p>Try another city, or raise a general request so matching donors can receive notifications.</p>
                    <div class="hero-actions compact-actions">
                        <a href="request.jsp" class="btn btn-primary btn-small">Raise General Request</a>
                    </div>
                </div>
            <% } %>
        </section>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
