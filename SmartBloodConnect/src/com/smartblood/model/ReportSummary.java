package com.smartblood.model;

public class ReportSummary {
    private int totalUsers;
    private int totalDonors;
    private int totalRequests;
    private int successfulDonations;
    private int emergencyRequests;

    public int getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(int totalUsers) {
        this.totalUsers = totalUsers;
    }

    public int getTotalDonors() {
        return totalDonors;
    }

    public void setTotalDonors(int totalDonors) {
        this.totalDonors = totalDonors;
    }

    public int getTotalRequests() {
        return totalRequests;
    }

    public void setTotalRequests(int totalRequests) {
        this.totalRequests = totalRequests;
    }

    public int getSuccessfulDonations() {
        return successfulDonations;
    }

    public void setSuccessfulDonations(int successfulDonations) {
        this.successfulDonations = successfulDonations;
    }

    public int getEmergencyRequests() {
        return emergencyRequests;
    }

    public void setEmergencyRequests(int emergencyRequests) {
        this.emergencyRequests = emergencyRequests;
    }
}
