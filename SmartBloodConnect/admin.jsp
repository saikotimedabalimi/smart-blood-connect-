<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.time.LocalDate, com.smartblood.model.User, com.smartblood.model.Donor, com.smartblood.model.Request, com.smartblood.model.ReportSummary" %>
<%
    List<User> users = (List<User>) request.getAttribute("users");
    List<Donor> donors = (List<Donor>) request.getAttribute("donors");
    List<Request> requests = (List<Request>) request.getAttribute("requests");
    ReportSummary summary = (ReportSummary) request.getAttribute("summary");
    String flashMessage = (String) session.getAttribute("flashMessage");
    String flashType = (String) session.getAttribute("flashType");
    if (flashMessage != null) {
        session.removeAttribute("flashMessage");
        session.removeAttribute("flashType");
    }

    int awaitingDonor = 0;
    int acceptedByDonor = 0;
    int completed = 0;
    int withFormalities = 0;
    if (requests != null) {
        for (Request bloodRequest : requests) {
            if ("Accepted by Donor".equalsIgnoreCase(bloodRequest.getStatus())) {
                acceptedByDonor++;
                if (bloodRequest.getContactName() != null && !bloodRequest.getContactName().isEmpty()) {
                    withFormalities++;
                }
            } else if ("Donation Completed".equalsIgnoreCase(bloodRequest.getStatus())) {
                completed++;
            } else if (!"Rejected by Donor".equalsIgnoreCase(bloodRequest.getStatus())
                    && !"No Donor Accepted".equalsIgnoreCase(bloodRequest.getStatus())) {
                awaitingDonor++;
            }
        }
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="page-shell">
    <div class="container">
        <div class="section-title left">
            <span>Admin control room</span>
            <h1>Manage donors, requests, and certificates</h1>
        </div>

        <% if (flashMessage != null) { %>
            <div class="message <%= "error".equals(flashType) ? "error" : "success" %>"><%= flashMessage %></div>
        <% } %>
        <% if (flashMessage == null && (request.getParameter("completed") != null || request.getParameter("deleted") != null)) { %>
            <div class="message success">Admin action completed successfully.</div>
        <% } %>

        <section class="stats-grid">
            <article class="card stat-card">
                <h3>Total Users</h3>
                <strong><%= summary == null ? 0 : summary.getTotalUsers() %></strong>
            </article>
            <article class="card stat-card">
                <h3>Total Donors</h3>
                <strong><%= summary == null ? 0 : summary.getTotalDonors() %></strong>
            </article>
            <article class="card stat-card">
                <h3>Awaiting Donor</h3>
                <strong><%= awaitingDonor %></strong>
            </article>
            <article class="card stat-card">
                <h3>Accepted by Donor</h3>
                <strong><%= acceptedByDonor %></strong>
            </article>
            <article class="card stat-card">
                <h3>Formalities Shared</h3>
                <strong><%= withFormalities %></strong>
            </article>
            <article class="card stat-card alert-card">
                <h3>Completed Donations</h3>
                <strong><%= completed %></strong>
            </article>
        </section>

        <section class="card table-card">
            <div class="table-header">
                <div>
                    <span class="eyebrow alt">User registry</span>
                    <h3>All Registered Users</h3>
                </div>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Phone</th>
                        <th>Type</th>
                        <th>City</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (users != null && !users.isEmpty()) {
                           for (User item : users) { %>
                        <tr>
                            <td><%= item.getId() %></td>
                            <td><%= item.getName() %></td>
                            <td><%= item.getEmail() %></td>
                            <td><%= item.getPhone() %></td>
                            <td><%= item.getAccountType() %></td>
                            <td><%= item.getCity() %></td>
                            <td>
                                <form action="admin/delete-user" method="post">
                                    <input type="hidden" name="userId" value="<%= item.getId() %>">
                                    <button type="submit" class="btn btn-outline btn-small">Delete</button>
                                </form>
                            </td>
                        </tr>
                    <%   }
                       } else { %>
                        <tr>
                            <td colspan="7" class="table-empty">No users found.</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </section>

        <div class="grid-2">
            <section class="card table-card">
                <div class="table-header">
                    <div>
                        <span class="eyebrow alt">Donor inventory</span>
                        <h3>Donor Availability</h3>
                    </div>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Name</th>
                            <th>Blood Group</th>
                            <th>City</th>
                            <th>Last Donation</th>
                            <th>Available</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (donors != null && !donors.isEmpty()) {
                               for (Donor donor : donors) { %>
                            <tr>
                                <td><%= donor.getName() %></td>
                                <td><span class="blood-mini"><%= donor.getBloodGroup() %></span></td>
                                <td><%= donor.getCity() %></td>
                                <td><%= donor.getLastDonationDate() == null ? "Not shared" : donor.getLastDonationDate() %></td>
                                <td><span class="status <%= donor.isAvailable() ? "approved" : "rejected" %>"><%= donor.isAvailable() ? "Yes" : "No" %></span></td>
                            </tr>
                        <%   }
                           } else { %>
                            <tr>
                                <td colspan="5" class="table-empty">No donor profiles found.</td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </section>

            <section class="card table-card">
                <div class="table-header">
                    <div>
                        <span class="eyebrow alt">Certificate workflow</span>
                        <h3>Issue Donation Certificate</h3>
                    </div>
                </div>
                <div class="admin-help-list">
                    <p>1. Wait for donor acceptance.</p>
                    <p>2. Confirm contact and procurer formalities are shared.</p>
                    <p>3. Mark donation complete and issue the certificate.</p>
                </div>
            </section>
        </div>

        <section class="card table-card">
            <div class="table-header">
                <div>
                    <span class="eyebrow alt">Full request lifecycle</span>
                    <h3>All Blood Requests</h3>
                </div>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Requester</th>
                        <th>Patient</th>
                        <th>Need</th>
                        <th>Donor / Routing</th>
                        <th>Formalities</th>
                        <th>Status</th>
                        <th>Admin Action</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (requests != null && !requests.isEmpty()) {
                           for (Request bloodRequest : requests) { %>
                        <tr>
                            <td>#<%= bloodRequest.getId() %></td>
                            <td>
                                <strong><%= bloodRequest.getRequesterName() %></strong>
                                <div class="table-sub"><%= bloodRequest.getCity() %></div>
                            </td>
                            <td><%= bloodRequest.getPatientName() %></td>
                            <td>
                                <span class="blood-mini"><%= bloodRequest.getBloodGroup() %></span>
                                <div class="table-sub"><%= bloodRequest.getUnitsRequired() %> unit(s) | <%= bloodRequest.isEmergency() ? "Emergency" : "Standard" %></div>
                            </td>
                            <td>
                                <% if (bloodRequest.getAssignedDonorName() != null && !bloodRequest.getAssignedDonorName().isEmpty()) { %>
                                    <strong><%= bloodRequest.getAssignedDonorName() %></strong>
                                    <div class="table-sub"><%= bloodRequest.getAssignedDonorPhone() == null ? "" : bloodRequest.getAssignedDonorPhone() %></div>
                                <% } else { %>
                                    <%= bloodRequest.getMatchedDonorSummary() == null ? "Not routed yet" : bloodRequest.getMatchedDonorSummary() %>
                                <% } %>
                            </td>
                            <td>
                                <div><strong><%= bloodRequest.getProcurementStatus() == null ? "Pending" : bloodRequest.getProcurementStatus() %></strong></div>
                                <% if (bloodRequest.getContactName() != null && !bloodRequest.getContactName().isEmpty()) { %>
                                    <div class="table-sub"><%= bloodRequest.getContactName() %> | <%= bloodRequest.getContactPhone() %></div>
                                <% } %>
                                <% if (bloodRequest.getHospitalName() != null && !bloodRequest.getHospitalName().isEmpty()) { %>
                                    <div class="table-sub"><%= bloodRequest.getHospitalName() %></div>
                                <% } %>
                            </td>
                            <td><span class="status <%= bloodRequest.getStatus().toLowerCase().replace(' ', '-') %>"><%= bloodRequest.getStatus() %></span></td>
                            <td class="action-cell action-stack">
                                <% if ("Accepted by Donor".equalsIgnoreCase(bloodRequest.getStatus())) { %>
                                    <form action="admin/complete-donation" method="post" class="inline-form">
                                        <input type="hidden" name="requestId" value="<%= bloodRequest.getId() %>">
                                        <div class="form-group">
                                            <label>Issuer Type</label>
                                            <select name="issuerType">
                                                <option value="GOVERNMENT">Government</option>
                                                <option value="UNIVERSITY">University</option>
                                                <option value="HOSPITAL">Hospital</option>
                                            </select>
                                        </div>
                                        <div class="form-group">
                                            <label>Issuer Name</label>
                                            <input type="text" name="issuerName" value="Smart Blood Connect Authority" required>
                                        </div>
                                        <div class="grid-2">
                                            <div class="form-group">
                                                <label>Issued On</label>
                                                <input type="date" name="issuedOn" value="<%= LocalDate.now() %>" required>
                                            </div>
                                            <div class="form-group">
                                                <label>Certificate No.</label>
                                                <input type="text" name="certificateNumber" placeholder="Auto-generate if blank">
                                            </div>
                                        </div>
                                        <button type="submit" class="btn btn-primary btn-small">Mark Complete + Issue</button>
                                    </form>
                                <% } else if (bloodRequest.isCertificateAvailable()) { %>
                                    <a href="certificate/download?requestId=<%= bloodRequest.getId() %>" class="btn btn-primary btn-small">Download Certificate</a>
                                    <div class="table-sub">Certificate No: <%= bloodRequest.getCertificateNumber() %></div>
                                <% } else { %>
                                    <span class="muted-copy">Monitor request flow</span>
                                <% } %>
                            </td>
                        </tr>
                    <%   }
                       } else { %>
                        <tr>
                            <td colspan="8" class="table-empty">No requests found.</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </section>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
