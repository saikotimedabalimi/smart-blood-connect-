<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%
    String selectedGroup = request.getParameter("group") == null ? "A+" : request.getParameter("group");
    java.util.Map<String, String> donorMatches = new java.util.LinkedHashMap<String, String>();
    donorMatches.put("A+", "A+, A-, O+, O-");
    donorMatches.put("A-", "A-, O-");
    donorMatches.put("B+", "B+, B-, O+, O-");
    donorMatches.put("B-", "B-, O-");
    donorMatches.put("AB+", "All blood groups");
    donorMatches.put("AB-", "AB-, A-, B-, O-");
    donorMatches.put("O+", "O+, O-");
    donorMatches.put("O-", "O- only");

    java.util.Map<String, String> canDonateTo = new java.util.LinkedHashMap<String, String>();
    canDonateTo.put("A+", "A+, AB+");
    canDonateTo.put("A-", "A+, A-, AB+, AB-");
    canDonateTo.put("B+", "B+, AB+");
    canDonateTo.put("B-", "B+, B-, AB+, AB-");
    canDonateTo.put("AB+", "AB+ only");
    canDonateTo.put("AB-", "AB+, AB-");
    canDonateTo.put("O+", "O+, A+, B+, AB+");
    canDonateTo.put("O-", "All blood groups");

    if (!donorMatches.containsKey(selectedGroup)) {
        selectedGroup = "A+";
    }
%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Blood Compatibility Guide</title>
    <link rel="stylesheet" href="css/style.css">
</head>
<body>
<%@ include file="header.jsp" %>

<main class="page-shell">
    <div class="container">
        <div class="section-title left">
            <span class="eyebrow">Compatibility helper</span>
            <h1>Check who can donate before you raise a request</h1>
        </div>

        <section class="compatibility-hero">
            <div class="card form-card">
                <h3>Recipient blood group</h3>
                <form action="compatibility.jsp" method="get" class="compatibility-form">
                    <div class="form-group">
                        <label for="group">Select blood group</label>
                        <select id="group" name="group">
                            <% for (String group : donorMatches.keySet()) { %>
                                <option value="<%= group %>" <%= group.equals(selectedGroup) ? "selected" : "" %>><%= group %></option>
                            <% } %>
                        </select>
                    </div>
                    <button type="submit" class="btn btn-primary full-width">Check Compatibility</button>
                </form>
            </div>

            <div class="card compatibility-result">
                <div class="blood-badge large-badge"><%= selectedGroup %></div>
                <div>
                    <span class="eyebrow alt">Best donor matches</span>
                    <h2><%= donorMatches.get(selectedGroup) %></h2>
                    <p>If you are a <strong><%= selectedGroup %></strong> donor, you can donate to: <%= canDonateTo.get(selectedGroup) %>.</p>
                </div>
            </div>
        </section>

        <section class="card table-card">
            <div class="table-header">
                <div>
                    <span class="eyebrow alt">Reference chart</span>
                    <h3>Blood Group Compatibility</h3>
                </div>
                <a href="search.jsp" class="btn btn-outline btn-small">Find Matching Donors</a>
            </div>
            <table class="data-table">
                <thead>
                    <tr>
                        <th>Recipient</th>
                        <th>Can Receive From</th>
                        <th>Can Donate To</th>
                    </tr>
                </thead>
                <tbody>
                    <% for (String group : donorMatches.keySet()) { %>
                        <tr class="<%= group.equals(selectedGroup) ? "highlight-row" : "" %>">
                            <td><span class="blood-mini"><%= group %></span></td>
                            <td><%= donorMatches.get(group) %></td>
                            <td><%= canDonateTo.get(group) %></td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </section>

        <section class="hero-band tips-band">
            <article class="card band-item">
                <h3>Universal donor</h3>
                <p>O- red blood cells are commonly treated as the universal emergency donor group.</p>
            </article>
            <article class="card band-item">
                <h3>Universal recipient</h3>
                <p>AB+ recipients can receive red blood cells from all listed blood groups.</p>
            </article>
            <article class="card band-item">
                <h3>Always confirm</h3>
                <p>Final compatibility must be confirmed by hospital blood-bank testing before transfusion.</p>
            </article>
        </section>
    </div>
</main>

<%@ include file="footer.jsp" %>
</body>
</html>
