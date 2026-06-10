<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.ArrayList, com.smartblood.dao.DonorDAO, com.smartblood.dao.ReportDAO, com.smartblood.dao.RequestDAO, com.smartblood.dao.BroadcastDAO, com.smartblood.model.Donor, com.smartblood.model.ReportSummary, com.smartblood.model.Request, com.smartblood.model.Broadcast" %>
<%
    List<Donor> publicDonors = new DonorDAO().searchDonors("", "");
    List<Request> allRequests = new RequestDAO().getAllRequests();
    List<Broadcast> globalBroadcasts = new BroadcastDAO().getAllActiveBroadcasts();
    List<Request> emergencyRequests = new ArrayList<Request>();
    if (allRequests != null) {
        for (Request item : allRequests) {
            if (item.isEmergency() && "Pending".equalsIgnoreCase(item.getStatus())) {
                emergencyRequests.add(item);
            }
        }
    }
    ReportSummary summary = new ReportDAO().getSummary();
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Smart Blood Connect</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .sos-feed { background: #fff5f5; padding: 2rem 0; border-bottom: 1px solid #fee2e2; }
        .sos-container { display: flex; flex-direction: column; gap: 1rem; }
        .sos-card { 
            background: white; border: 1px solid #fecaca; border-radius: 8px; padding: 1.25rem; 
            display: flex; justify-content: space-between; align-items: center;
            box-shadow: 0 2px 4px rgba(0,0,0,0.05);
            transition: transform 0.2s;
        }
        .sos-card:hover { transform: translateY(-2px); box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
        .sos-info h4 { margin: 0 0 0.5rem 0; color: #b91c1c; display: flex; align-items: center; gap: 0.5rem; }
        .sos-info p { margin: 0; color: #4b5563; font-style: italic; }
        .sos-meta { display: flex; gap: 1rem; margin-top: 0.5rem; font-size: 0.85rem; color: #6b7280; }
        .sos-badge { background: #ef4444; color: white; padding: 0.5rem 1rem; border-radius: 9999px; font-weight: bold; font-size: 1.1rem; }
        @media (max-width: 768px) {
            .sos-card { flex-direction: column; align-items: flex-start; gap: 1rem; }
            .sos-badge { align-self: flex-end; }
        }
    </style>
</head>
<body>
<%@ include file="header.jsp" %>

<% if (globalBroadcasts != null && !globalBroadcasts.isEmpty()) { %>
<section class="sos-feed">
    <div class="container">
        <div class="section-title left">
            <span class="eyebrow" style="color: #ef4444;">Live Alerts</span>
            <h2>Global Blood SOS Feed</h2>
        </div>
        <div class="sos-container">
            <% for (Broadcast b : globalBroadcasts) { %>
                <div class="sos-card">
                    <div class="sos-info">
                        <h4>🚨 Urgent Request from <%= b.getUserName() %></h4>
                        <p>"<%= b.getMessage() %>"</p>
                        <div class="sos-meta">
                            <span>📍 <%= b.getCity() %></span>
                            <span>⏰ <%= b.getCreatedAt() %></span>
                        </div>
                    </div>
                    <div class="sos-badge"><%= b.getBloodGroup() %></div>
                </div>
            <% } %>
        </div>
    </div>
</section>
<% } %>

<section class="hero-section">
    <div class="container hero-grid">
        <div class="hero-copy">
            <span class="eyebrow">Public access before login</span>
            <h1>Find donors, understand the system, and request help faster.</h1>
            <p>
                Smart Blood Connect lets users search available blood, send direct requests to donors, track donor
                accept or reject decisions, share hospital formalities, and download donation certificates after completion.
            </p>
            <div class="hero-actions">
                <a href="search" class="btn btn-primary">Browse Donors</a>
                <a href="compatibility.jsp" class="btn btn-outline">Check Compatibility</a>
                <a href="login.jsp" class="btn btn-outline">Login</a>
            </div>
            <div class="visitor-banner">
                <strong>Visitor mode:</strong>
                You can view donors and search the system without logging in. Login is only needed for dashboard actions and request management.
            </div>
        </div>

        <div class="hero-panel">
            <span class="eyebrow">Live system snapshot</span>
            <div class="stats-grid">
                <div class="stat-chip">
                    <span>Total Donors</span>
                    <strong><%= summary.getTotalDonors() %></strong>
                </div>
                <div class="stat-chip">
                    <span>Total Requests</span>
                    <strong><%= summary.getTotalRequests() %></strong>
                </div>
                <div class="stat-chip">
                    <span>Emergency Cases</span>
                    <strong><%= summary.getEmergencyRequests() %></strong>
                </div>
                <div class="stat-chip">
                    <span>Successful Matches</span>
                    <strong><%= summary.getSuccessfulDonations() %></strong>
                </div>
            </div>
        </div>
    </div>
</section>

<section class="section-light">
    <div class="container">
        <div class="section-title left">
            <span class="eyebrow">Emergency board</span>
            <h2>Pending urgent requests</h2>
        </div>
        <div class="results-grid">
            <% if (!emergencyRequests.isEmpty()) {
                   int urgentCount = Math.min(emergencyRequests.size(), 3);
                   for (int i = 0; i < urgentCount; i++) {
                       Request bloodRequest = emergencyRequests.get(i);
            %>
                <article class="card emergency-card">
                    <div class="card-topline">
                        <span class="blood-badge"><%= bloodRequest.getBloodGroup() %></span>
                        <span class="status pending">Emergency</span>
                    </div>
                    <h3><%= bloodRequest.getCity() %></h3>
                    <p><strong>Hospital / Area:</strong> <%= bloodRequest.getLocation() %></p>
                    <p><strong>Units needed:</strong> <%= bloodRequest.getUnitsRequired() %></p>
                    <p><strong>Current status:</strong> <%= bloodRequest.getStatus() %></p>
                    <div class="hero-actions compact-actions">
                        <a href="search?bloodGroup=<%= java.net.URLEncoder.encode(bloodRequest.getBloodGroup(), "UTF-8") %>&city=<%= java.net.URLEncoder.encode(bloodRequest.getCity(), "UTF-8") %>" class="btn btn-primary btn-small">Find Donors</a>
                        <a href="request.jsp" class="btn btn-outline btn-small">Raise Request</a>
                    </div>
                </article>
            <%     }
               } else { %>
                <article class="card empty-state">
                    <h3>No emergency requests pending right now</h3>
                    <p>The public board will highlight urgent pending cases as soon as they are submitted.</p>
                </article>
            <% } %>
        </div>
    </div>
</section>

<section class="section-light">
    <div class="container">
        <div class="section-title center">
            <span class="eyebrow">Simple flow</span>
            <h2>Full donor-to-certificate workflow</h2>
        </div>
        <div class="hero-band">
            <article class="card band-item">
                <h3>1. Visit without login</h3>
                <p>See the platform clearly, review donor visibility, and understand what the system does.</p>
            </article>
            <article class="card band-item">
                <h3>2. Search live donors</h3>
                <p>Use the public donor search to find available compatible donors and send direct requests.</p>
            </article>
            <article class="card band-item">
                <h3>3. Check compatibility</h3>
                <p>Use the compatibility guide before searching so you know which groups can help fastest.</p>
            </article>
            <article class="card band-item">
                <h3>4. Manage the full flow</h3>
                <p>Donors accept or reject, requesters share formalities, admin issues certificates, and alerts reach users fast.</p>
            </article>
        </div>
    </div>
</section>

<section class="section-dark public-list">
    <div class="container">
        <div class="section-title left">
            <span class="eyebrow">Visible to all visitors</span>
            <h2>Featured donors</h2>
        </div>
        <div class="results-grid">
            <% if (publicDonors != null && !publicDonors.isEmpty()) {
                   int featuredCount = Math.min(publicDonors.size(), 6);
                   for (int i = 0; i < featuredCount; i++) {
                       Donor donor = publicDonors.get(i);
            %>
                <article class="card donor-card">
                    <div class="blood-badge"><%= donor.getBloodGroup() %></div>
                    <h3><%= donor.getName() %></h3>
                    <p><strong>City:</strong> <%= donor.getCity() %></p>
                    <p><strong>Location:</strong> <%= donor.getLocation() %></p>
                    <p><strong>Status:</strong> <%= donor.isAvailable() ? "Available" : "Unavailable" %></p>
                </article>
            <%     }
               } else { %>
                <div class="card empty-state">
                    <h3>No public donor data yet</h3>
                    <p>Register donors to make this homepage more useful for visitors.</p>
                </div>
            <% } %>
        </div>
    </div>
</section>

<%@ include file="footer.jsp" %>
</body>
</html>
