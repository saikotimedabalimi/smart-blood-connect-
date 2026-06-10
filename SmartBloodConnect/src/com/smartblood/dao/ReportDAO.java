package com.smartblood.dao;

import com.smartblood.model.ReportSummary;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class ReportDAO {
    public ReportSummary getSummary() {
        SchemaManager.ensureSchema();
        ReportSummary summary = new ReportSummary();
        summary.setTotalUsers(count("SELECT COUNT(*) FROM users"));
        summary.setTotalDonors(count("SELECT COUNT(*) FROM donors"));
        summary.setTotalRequests(count("SELECT COUNT(*) FROM requests"));
        summary.setSuccessfulDonations(count("SELECT COUNT(*) FROM requests WHERE status = 'Donation Completed'"));
        summary.setEmergencyRequests(count("SELECT COUNT(*) FROM requests WHERE emergency = 1 AND status <> 'Donation Completed'"));
        return summary;
    }

    private int count(String sql) {
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return 0;
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }
}
