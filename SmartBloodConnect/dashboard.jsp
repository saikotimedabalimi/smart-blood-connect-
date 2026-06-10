<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.List, java.util.ArrayList, java.time.LocalDate, java.time.temporal.ChronoUnit, java.time.format.DateTimeParseException, com.smartblood.model.User, com.smartblood.model.Donor, com.smartblood.model.Request, com.smartblood.model.Notification, com.smartblood.model.Broadcast" %>
<%
    User user = (User) session.getAttribute("user");
    if (user == null) {
        response.sendRedirect("login.jsp?error=login");
        return;
    }

    Donor donorProfile = (Donor) request.getAttribute("donorProfile");
    List<Request> requests = (List<Request>) request.getAttribute("requests");
    List<Request> donorRequests = (List<Request>) request.getAttribute("donorRequests");
    List<Notification> notifications = (List<Notification>) request.getAttribute("notifications");
    List<Broadcast> broadcasts = (List<Broadcast>) request.getAttribute("broadcasts");
    Integer unreadCount = (Integer) request.getAttribute("unreadCount");
    unreadCount = unreadCount == null ? 0 : unreadCount;

    String flashMessage = (String) session.getAttribute("flashMessage");
    String flashType = (String) session.getAttribute("flashType");
    if (flashMessage != null) {
        session.removeAttribute("flashMessage");
        session.removeAttribute("flashType");
    }

    int totalRequests = requests == null ? 0 : requests.size();
    int acceptedRequests = 0;
    int completedRequests = 0;
    int pendingRequests = 0;
    int donorInboxPending = 0;
    List<Request> formalityRequests = new ArrayList<Request>();
    List<Request> certificateRequests = new ArrayList<Request>();

    if (requests != null) {
        for (Request bloodRequest : requests) {
            if ("Accepted by Donor".equalsIgnoreCase(bloodRequest.getStatus())) {
                acceptedRequests++;
                formalityRequests.add(bloodRequest);
            } else if ("Donation Completed".equalsIgnoreCase(bloodRequest.getStatus())) {
                completedRequests++;
            } else if (!"Rejected by Donor".equalsIgnoreCase(bloodRequest.getStatus())
                    && !"No Donor Accepted".equalsIgnoreCase(bloodRequest.getStatus())) {
                pendingRequests++;
            }
        }
    }

    if (donorRequests != null) {
        for (Request donorRequest : donorRequests) {
            if ("PENDING".equalsIgnoreCase(donorRequest.getDonorResponseStatus())) {
                donorInboxPending++;
            }
            if (donorRequest.isCertificateAvailable() && "Donation Completed".equalsIgnoreCase(donorRequest.getStatus())) {
                certificateRequests.add(donorRequest);
            }
        }
    }

    String donorReadinessTitle = "Receiver account";
    String donorReadinessCopy = "Register as donor to start receiving blood-needs notifications and incoming requests.";
    String donorReadinessStatus = "Info";
    if (donorProfile != null) {
        donorReadinessStatus = donorProfile.isAvailable() ? "Available" : "Paused";
        donorReadinessTitle = donorProfile.isAvailable() ? "Ready for blood requests" : "Temporarily hidden";
        donorReadinessCopy = "Update your availability and recent donation date to stay visible.";
        if (donorProfile.getLastDonationDate() != null && !donorProfile.getLastDonationDate().trim().isEmpty()) {
            try {
                LocalDate lastDonation = LocalDate.parse(donorProfile.getLastDonationDate());
                LocalDate nextEligible = lastDonation.plusDays(90);
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), nextEligible);
                if (daysLeft <= 0) {
                    donorReadinessTitle = "Eligible based on last donation";
                    donorReadinessCopy = "You can accept new requests again based on the 90-day rest window.";
                } else {
                    donorReadinessTitle = "Rest period active";
                    donorReadinessCopy = "Estimated next eligible date: " + nextEligible + " (" + daysLeft + " days left).";
                }
            } catch (DateTimeParseException ignored) {
                donorReadinessCopy = "The last donation date is saved but could not be parsed.";
            }
        }
    }

    boolean showSuccess = flashMessage == null
            && ("updated".equals(request.getParameter("profile"))
            || "updated".equals(request.getParameter("donor"))
            || "success".equals(request.getParameter("request"))
            || "success".equals(request.getParameter("donorAction"))
            || "updated".equals(request.getParameter("formalities"))
            || "success".equals(request.getParameter("broadcast")));
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Dashboard</title>
    <link rel="stylesheet" href="css/style.css">
    <style>
        .broadcast-banner {
            background: linear-gradient(135deg, #ff4d4d, #b30000);
            color: white;
            padding: 1.5rem;
            border-radius: 12px;
            margin-bottom: 2rem;
            box-shadow: 0 8px 32px rgba(255, 77, 77, 0.2);
        }
        .broadcast-item {
            background: rgba(255, 255, 255, 0.1);
            padding: 1rem;
            border-radius: 8px;
            margin-top: 1rem;
            border-left: 4px solid #fff;
        }
        .broadcast-item strong { display: block; font-size: 1.1rem; margin-bottom: 0.25rem; }
        .broadcast-item p { margin: 0.5rem 0; opacity: 0.9; }
        .broadcast-meta { font-size: 0.85rem; opacity: 0.7; display: flex; gap: 1rem; }
        
        .sos-form { margin-top: 2rem; }
        .sos-grid { display: grid; grid-template-columns: 2fr 1fr 1fr auto; gap: 1rem; align-items: end; }
        @media (max-width: 768px) { .sos-grid { grid-template-columns: 1fr; } }
    </style>
</head>
<body data-notification-feed="notifications/feed">
<%@ include file="header.jsp" %>

<main class="page-shell">
    <div class="container">
        <div class="section-title left">
            <span>User command center</span>
            <h1>Welcome, <%= user.getName() %></h1>
        </div>

        <% if (flashMessage != null) { %>
            <div class="message <%= "error".equals(flashType) ? "error" : "success" %>"><%= flashMessage %></div>
        <% } %>
        <% if (showSuccess) { %>
            <div class="message success">Your dashboard action was completed successfully.</div>
        <% } %>

        <!-- Global SOS Broadcasts -->
        <section class="broadcast-banner">
            <div class="table-header">
                <div style="color: white;">
                    <span class="eyebrow" style="color: rgba(255,255,255,0.8);">Urgent Public Requests</span>
                    <h3 style="color: white; margin: 0;">Global Blood SOS</h3>
                </div>
            </div>
            
            <div class="broadcast-list">
                <% if (broadcasts != null && !broadcasts.isEmpty()) {
                       for (Broadcast b : broadcasts) { %>
                    <article class="broadcast-item">
                        <strong><%= b.getUserName() %> needs <%= b.getBloodGroup() %> blood in <%= b.getCity() %></strong>
                        <p>"<%= b.getMessage() %>"</p>
                        <div class="broadcast-meta">
                            <span>📍 <%= b.getCity() %></span>
                            <span>🩸 <%= b.getBloodGroup() %></span>
                            <span>⏰ <%= b.getCreatedAt() %></span>
                        </div>
                    </article>
                <%   }
                   } else { %>
                    <p style="margin-top: 1rem; opacity: 0.8;">No urgent broadcasts at the moment. If you can't find blood, use the form below to alert all users.</p>
                <% } %>
            </div>

            <div class="sos-form">
                <h4 style="color: white; margin-bottom: 1rem;">Post an Urgent "I Need Blood" Message</h4>
                <form action="broadcast/post" method="post" class="sos-grid">
                    <div class="form-group">
                        <label style="color: white;">Urgent Message</label>
                        <input type="text" name="message" placeholder="e.g. Emergency at City Hospital, need 2 units" required style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white;">
                    </div>
                    <div class="form-group">
                        <label style="color: white;">Blood Group</label>
                        <select name="bloodGroup" required style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white;">
                            <option value="">Select</option>
                            <option value="A+">A+</option><option value="A-">A-</option>
                            <option value="B+">B+</option><option value="B-">B-</option>
                            <option value="AB+">AB+</option><option value="AB-">AB-</option>
                            <option value="O+">O+</option><option value="O-">O-</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <label style="color: white;">City</label>
                        <input type="text" name="city" value="<%= user.getCity() %>" required style="background: rgba(255,255,255,0.2); border: 1px solid rgba(255,255,255,0.3); color: white;">
                    </div>
                    <button type="submit" class="btn btn-primary" style="background: white; color: #b30000; border: none;">Broadcast SOS</button>
                </form>
            </div>
        </section>

        <section class="stats-grid dashboard-summary">
            <article class="card stat-card">
                <h3>Total Requests</h3>
                <strong><%= totalRequests %></strong>
            </article>
            <article class="card stat-card">
                <h3>Accepted by Donor</h3>
                <strong><%= acceptedRequests %></strong>
            </article>
            <article class="card stat-card">
                <h3>Completed Donations</h3>
                <strong><%= completedRequests %></strong>
            </article>
            <article class="card stat-card alert-card">
                <h3>Unread Alerts</h3>
                <strong><%= unreadCount %></strong>
            </article>
        </section>

        <section class="card readiness-card">
            <div>
                <span class="eyebrow alt">Donor readiness</span>
                <h3><%= donorReadinessTitle %></h3>
                <p><%= donorReadinessCopy %></p>
            </div>
            <span class="status <%= donorProfile != null && donorProfile.isAvailable() ? "approved" : "pending" %>"><%= donorReadinessStatus %></span>
        </section>

        <div class="grid-2">
            <section class="card form-card">
                <h3>Profile Details</h3>
                <form action="profile/update" method="post">
                    <div class="form-group">
                        <label for="name">Name</label>
                        <input id="name" type="text" name="name" value="<%= user.getName() %>" required>
                    </div>
                    <div class="grid-2">
                        <div class="form-group">
                            <label for="phone">Phone</label>
                            <input id="phone" type="text" name="phone" value="<%= user.getPhone() %>" required>
                        </div>
                        <div class="form-group">
                            <label for="accountType">Account Type</label>
                            <select id="accountType" name="accountType" required onchange="toggleBloodGroup(this.value)">
                                <option value="DONOR" <%= "DONOR".equals(user.getAccountType()) ? "selected" : "" %>>Donor</option>
                                <option value="RECEIVER" <%= "RECEIVER".equals(user.getAccountType()) ? "selected" : "" %>>Receiver</option>
                            </select>
                        </div>
                    </div>
                    <div id="bloodGroupSection" class="form-group" style="display: <%= "DONOR".equals(user.getAccountType()) ? "none" : "block" %>;">
                         <label for="bloodGroup">Select Blood Group (to switch to Donor)</label>
                         <select id="bloodGroup" name="bloodGroup">
                             <option value="">Select Blood Group</option>
                             <option value="A+">A+</option><option value="A-">A-</option>
                             <option value="B+">B+</option><option value="B-">B-</option>
                             <option value="AB+">AB+</option><option value="AB-">AB-</option>
                             <option value="O+">O+</option><option value="O-">O-</option>
                         </select>
                         <div class="table-sub">Required only if you are changing your account type to Donor.</div>
                    </div>
                    <div class="grid-2">
                        <div class="form-group">
                            <label for="city">City</label>
                            <input id="city" type="text" name="city" value="<%= user.getCity() %>" required>
                        </div>
                        <div class="form-group">
                            <label for="location">Location</label>
                            <input id="location" type="text" name="location" value="<%= user.getLocation() %>" required>
                        </div>
                    </div>
                    <div class="grid-2">
                        <div class="form-group">
                            <label for="password">New Password</label>
                            <input id="password" type="password" name="password" placeholder="Leave blank to keep current">
                        </div>
                        <div class="form-group">
                            <label for="confirmPassword">Confirm Password</label>
                            <input id="confirmPassword" type="password" name="confirmPassword" placeholder="Confirm new password">
                        </div>
                    </div>
                    <button type="submit" class="btn btn-primary full-width">Update Profile</button>
                </form>
                <script>
                    function toggleBloodGroup(val) {
                        const section = document.getElementById('bloodGroupSection');
                        const currentType = '<%= user.getAccountType() %>';
                        if (val === 'DONOR' && currentType !== 'DONOR') {
                            section.style.display = 'block';
                        } else {
                            section.style.display = 'none';
                        }
                    }
                </script>
            </section>

            <section class="card notification-panel">
                <div class="table-header">
                    <div>
                        <span class="eyebrow alt">Push-like alerts</span>
                        <h3>Notification Center</h3>
                    </div>
                    <button type="button" class="btn btn-outline btn-small" data-enable-notifications>Enable Browser Alerts</button>
                </div>
                <div class="notification-list" id="notification-list">
                    <% if (notifications != null && !notifications.isEmpty()) {
                           for (Notification notification : notifications) { %>
                        <article class="notification-item" data-notification-id="<%= notification.getId() %>">
                            <div>
                                <strong><%= notification.getTitle() %></strong>
                                <p><%= notification.getMessage() %></p>
                            </div>
                            <span><%= notification.getCreatedAt() %></span>
                        </article>
                    <%   }
                       } else { %>
                        <div class="empty-note">No notifications yet. New blood needs and donor actions will appear here.</div>
                    <% } %>
                </div>
            </section>
        </div>

        <div class="grid-2">
            <section class="card form-card">
                <h3>Donor Availability</h3>
                <% if (donorProfile != null) { %>
                    <form action="donor/availability" method="post">
                        <div class="grid-2">
                            <div class="form-group">
                                <label>Blood Group</label>
                                <input type="text" value="<%= donorProfile.getBloodGroup() %>" disabled>
                            </div>
                            <div class="form-group">
                                <label>Inbox Pending</label>
                                <input type="text" value="<%= donorInboxPending %> request(s)" disabled>
                            </div>
                        </div>
                        <div class="grid-2">
                            <div class="form-group">
                                <label>City</label>
                                <input type="text" value="<%= donorProfile.getCity() %>" disabled>
                            </div>
                            <div class="form-group">
                                <label>Location</label>
                                <input type="text" value="<%= donorProfile.getLocation() %>" disabled>
                            </div>
                        </div>
                        <div class="form-group">
                            <label for="lastDonationDate">Last Donation Date</label>
                            <input id="lastDonationDate" type="date" name="lastDonationDate" value="<%= donorProfile.getLastDonationDate() == null ? "" : donorProfile.getLastDonationDate() %>">
                        </div>
                        <div class="checkbox-row">
                            <input id="available" type="checkbox" name="available" <%= donorProfile.isAvailable() ? "checked" : "" %>>
                            <label for="available">Visible for new blood requests</label>
                        </div>
                        <button type="submit" class="btn btn-primary full-width">Save Donor Availability</button>
                    </form>
                <% } else { %>
                    <div class="empty-note">
                        This account is not registered as a donor, so donor inbox, acceptance actions, and certificates are hidden.
                    </div>
                <% } %>
            </section>
        </div>

        <% if (formalityRequests != null && !formalityRequests.isEmpty()) { %>
            <section class="card procurement-board">
                <div class="table-header">
                    <div>
                        <span class="eyebrow alt">Contact and procurer formalities</span>
                        <h3>Share Hospital Details After Donor Acceptance</h3>
                    </div>
                </div>
                <div class="procurement-grid">
                    <% for (Request bloodRequest : formalityRequests) { %>
                        <article class="card inner-card">
                            <div class="card-topline">
                                <div>
                                    <h4>Request #<%= bloodRequest.getId() %> for <%= bloodRequest.getPatientName() %></h4>
                                    <p><strong>Accepted Donor:</strong> <%= bloodRequest.getAssignedDonorName() == null ? "Assigned donor" : bloodRequest.getAssignedDonorName() %></p>
                                </div>
                                <span class="status approved"><%= bloodRequest.getProcurementStatus() == null ? "Awaiting details" : bloodRequest.getProcurementStatus() %></span>
                            </div>
                            <form action="request/formalities" method="post">
                                <input type="hidden" name="requestId" value="<%= bloodRequest.getId() %>">
                                <div class="grid-2">
                                    <div class="form-group">
                                        <label>Contact Person</label>
                                        <input type="text" name="contactName" value="<%= bloodRequest.getContactName() == null ? user.getName() : bloodRequest.getContactName() %>" required>
                                    </div>
                                    <div class="form-group">
                                        <label>Contact Phone</label>
                                        <input type="text" name="contactPhone" value="<%= bloodRequest.getContactPhone() == null ? user.getPhone() : bloodRequest.getContactPhone() %>" required>
                                    </div>
                                </div>
                                <div class="grid-2">
                                    <div class="form-group">
                                        <label>Hospital / Blood Bank</label>
                                        <input type="text" name="hospitalName" value="<%= bloodRequest.getHospitalName() == null ? bloodRequest.getLocation() : bloodRequest.getHospitalName() %>" required>
                                    </div>
                                    <div class="form-group">
                                        <label>Required By</label>
                                        <input type="datetime-local" name="requiredBy" value="<%= bloodRequest.getRequiredBy() == null ? "" : bloodRequest.getRequiredBy() %>" required>
                                    </div>
                                </div>
                                <div class="form-group">
                                    <label>Formalities / Documents</label>
                                    <textarea name="formalityNotes" rows="4" placeholder="Mention blood bank slip, ID proof, reporting desk, or procurer details."><%= bloodRequest.getFormalityNotes() == null ? "" : bloodRequest.getFormalityNotes() %></textarea>
                                </div>
                                <button type="submit" class="btn btn-primary full-width">Save Contact and Formalities</button>
                            </form>
                        </article>
                    <% } %>
                </div>
            </section>
        <% } %>

        <section class="card table-card">
            <div class="table-header">
                <div>
                    <span class="eyebrow alt">Requester side</span>
                    <h3>Your Blood Requests</h3>
                </div>
                <a href="request.jsp" class="btn btn-outline btn-small">Raise New Request</a>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>ID</th>
                        <th>Patient</th>
                        <th>Need</th>
                        <th>Donor Route</th>
                        <th>Status</th>
                        <th>Formalities</th>
                        <th>Emergency</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (requests != null && !requests.isEmpty()) {
                           for (Request bloodRequest : requests) { %>
                        <tr>
                            <td>#<%= bloodRequest.getId() %></td>
                            <td>
                                <strong><%= bloodRequest.getPatientName() %></strong>
                                <div class="table-sub"><%= bloodRequest.getCity() %></div>
                            </td>
                            <td>
                                <span class="blood-mini"><%= bloodRequest.getBloodGroup() %></span>
                                <div class="table-sub"><%= bloodRequest.getUnitsRequired() %> unit(s)</div>
                            </td>
                            <td>
                                <%= bloodRequest.getMatchedDonorSummary() == null ? "Waiting to route" : bloodRequest.getMatchedDonorSummary() %>
                            </td>
                            <td>
                                <span class="status <%= bloodRequest.getStatus().toLowerCase().replace(' ', '-') %>"><%= bloodRequest.getStatus() %></span>
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
                            <td><%= bloodRequest.isEmergency() ? "Yes" : "No" %></td>
                        </tr>
                    <%   }
                       } else { %>
                        <tr>
                            <td colspan="7" class="table-empty">No blood requests submitted yet.</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </section>

        <% if (donorProfile != null) { %>
            <section class="card table-card">
                <div class="table-header">
                    <div>
                        <span class="eyebrow alt">Donor side</span>
                        <h3>Incoming Requests for You</h3>
                    </div>
                </div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>Request</th>
                            <th>Requester</th>
                            <th>Patient</th>
                            <th>Need</th>
                            <th>Formalities</th>
                            <th>Your Response</th>
                            <th>Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <% if (donorRequests != null && !donorRequests.isEmpty()) {
                               for (Request donorRequest : donorRequests) { %>
                            <tr>
                                <td>
                                    <strong>#<%= donorRequest.getId() %></strong>
                                    <div class="table-sub"><%= donorRequest.getCity() %></div>
                                </td>
                                <td><%= donorRequest.getRequesterName() %></td>
                                <td><%= donorRequest.getPatientName() %></td>
                                <td>
                                    <span class="blood-mini"><%= donorRequest.getBloodGroup() %></span>
                                    <div class="table-sub"><%= donorRequest.getUnitsRequired() %> unit(s) | <%= donorRequest.isEmergency() ? "Emergency" : "Standard" %></div>
                                </td>
                                <td>
                                    <div><strong><%= donorRequest.getProcurementStatus() == null ? "Awaiting" : donorRequest.getProcurementStatus() %></strong></div>
                                    <% if (donorRequest.getContactName() != null && !donorRequest.getContactName().isEmpty()) { %>
                                        <div class="table-sub"><%= donorRequest.getContactName() %> | <%= donorRequest.getContactPhone() %></div>
                                    <% } %>
                                    <% if (donorRequest.getHospitalName() != null && !donorRequest.getHospitalName().isEmpty()) { %>
                                        <div class="table-sub"><%= donorRequest.getHospitalName() %></div>
                                    <% } %>
                                </td>
                                <td>
                                    <span class="status <%= donorRequest.getStatus().toLowerCase().replace(' ', '-') %>"><%= donorRequest.getDonorResponseStatus() == null ? donorRequest.getStatus() : donorRequest.getDonorResponseStatus() %></span>
                                </td>
                                <td class="action-cell">
                                    <% if ("PENDING".equalsIgnoreCase(donorRequest.getDonorResponseStatus())
                                            && !"Donation Completed".equalsIgnoreCase(donorRequest.getStatus())) { %>
                                        <form action="donor/request-action" method="post">
                                            <input type="hidden" name="requestId" value="<%= donorRequest.getId() %>">
                                            <input type="hidden" name="action" value="ACCEPT">
                                            <button type="submit" class="btn btn-primary btn-small">Accept</button>
                                        </form>
                                        <form action="donor/request-action" method="post">
                                            <input type="hidden" name="requestId" value="<%= donorRequest.getId() %>">
                                            <input type="hidden" name="action" value="REJECT">
                                            <button type="submit" class="btn btn-outline btn-small">Reject</button>
                                        </form>
                                    <% } else if (donorRequest.isCertificateAvailable()
                                            && "Donation Completed".equalsIgnoreCase(donorRequest.getStatus())) { %>
                                        <a href="certificate/download?requestId=<%= donorRequest.getId() %>" class="btn btn-primary btn-small">Download Certificate</a>
                                    <% } else { %>
                                        <span class="muted-copy">No action pending</span>
                                    <% } %>
                                </td>
                            </tr>
                        <%   }
                           } else { %>
                            <tr>
                                <td colspan="7" class="table-empty">No incoming donor requests yet.</td>
                            </tr>
                        <% } %>
                    </tbody>
                </table>
            </section>

            <section class="card certificate-board">
                <div class="table-header">
                    <div>
                        <span class="eyebrow alt">Downloadable proof</span>
                        <h3>Donation Certificates</h3>
                    </div>
                </div>
                <div class="certificate-grid">
                    <% if (certificateRequests != null && !certificateRequests.isEmpty()) {
                           for (Request certificateRequest : certificateRequests) { %>
                        <article class="card certificate-card">
                            <span class="eyebrow">Issued Certificate</span>
                            <h4>Request #<%= certificateRequest.getId() %></h4>
                            <p>Patient: <strong><%= certificateRequest.getPatientName() %></strong></p>
                            <p>Certificate No: <strong><%= certificateRequest.getCertificateNumber() %></strong></p>
                            <a href="certificate/download?requestId=<%= certificateRequest.getId() %>" class="btn btn-primary full-width">Download Certificate</a>
                        </article>
                    <%   }
                       } else { %>
                        <div class="empty-note">Certificates appear here after admin marks a donation as completed.</div>
                    <% } %>
                </div>
            </section>
        <% } %>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
