package com.smartblood.dao;

import com.smartblood.model.Donor;
import com.smartblood.util.BloodCompatibility;
import com.smartblood.util.ValidationUtils;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;

public class DonorDAO {
    public void createDonorProfile(int userId, String bloodGroup, String location, String city, String lastDonationDate)
            throws Exception {
        SchemaManager.ensureSchema();
        String sql = "INSERT INTO donors(user_id, blood_group, location, city, last_donation_date, available) "
                + "VALUES(?,?,?,?,?,1) "
                + "ON DUPLICATE KEY UPDATE blood_group=VALUES(blood_group), location=VALUES(location), "
                + "city=VALUES(city), last_donation_date=VALUES(last_donation_date)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return;
            }
            statement.setInt(1, userId);
            statement.setString(2, bloodGroup);
            statement.setString(3, location);
            statement.setString(4, city);
            setOptionalDate(statement, 5, lastDonationDate);
            statement.executeUpdate();
        }
    }

    public Donor findByUserId(int userId) {
        SchemaManager.ensureSchema();
        String sql = "SELECT d.*, u.name, u.email, u.phone "
                + "FROM donors d JOIN users u ON u.id = d.user_id WHERE d.user_id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return null;
            }
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapDonor(resultSet) : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public List<Donor> searchDonors(String bloodGroup, String city) {
        return searchDonorsInternal(bloodGroup, city, true, null);
    }

    public List<Donor> getAllDonors() {
        return searchDonorsInternal(null, null, false, null);
    }

    public void updateAvailability(int userId, boolean available, String lastDonationDate) {
        SchemaManager.ensureSchema();
        String sql = "UPDATE donors SET available=?, last_donation_date=? WHERE user_id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return;
            }
            statement.setBoolean(1, available);
            setOptionalDate(statement, 2, lastDonationDate);
            statement.setInt(3, userId);
            statement.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public String buildMatchSummary(String bloodGroup, String city) {
        List<Donor> donors = getTargetDonors(bloodGroup, city, 0, false);
        if (donors.isEmpty()) {
            return "No compatible donor is currently marked available in this city.";
        }

        int previewCount = Math.min(donors.size(), 3);
        StringJoiner joiner = new StringJoiner(", ");
        for (int i = 0; i < previewCount; i++) {
            Donor donor = donors.get(i);
            joiner.add(donor.getName() + " (" + donor.getPhone() + ")");
        }

        if (donors.size() > previewCount) {
            return joiner + " +" + (donors.size() - previewCount) + " more compatible donors";
        }
        return joiner.toString();
    }

    public List<Donor> getTargetDonors(String bloodGroup, String city, int selectedDonorUserId, boolean availableOnly) {
        return searchDonorsInternal(bloodGroup, city, availableOnly, selectedDonorUserId > 0 ? selectedDonorUserId : null);
    }

    public Donor findAvailableDonorByUserId(int userId) {
        List<Donor> donors = searchDonorsInternal(null, null, true, userId);
        return donors.isEmpty() ? null : donors.get(0);
    }

    private List<Donor> searchDonorsInternal(String bloodGroup, String city, boolean availableOnly, Integer donorUserId) {
        SchemaManager.ensureSchema();
        List<Donor> donors = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
                "SELECT d.*, u.name, u.email, u.phone "
                        + "FROM donors d JOIN users u ON u.id = d.user_id WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (availableOnly) {
            sql.append(" AND d.available = 1");
        }
        if (donorUserId != null) {
            sql.append(" AND d.user_id = ?");
            params.add(donorUserId);
        }
        String normalizedCity = ValidationUtils.trimToEmpty(city);
        if (!normalizedCity.isEmpty()) {
            sql.append(" AND LOWER(d.city) LIKE ?");
            params.add("%" + normalizedCity.toLowerCase() + "%");
        }

        Set<String> compatibleGroups = BloodCompatibility.getCompatibleDonorGroups(bloodGroup);
        if (!compatibleGroups.isEmpty()) {
            sql.append(" AND d.blood_group IN (");
            int index = 0;
            for (String ignored : compatibleGroups) {
                if (index++ > 0) {
                    sql.append(',');
                }
                sql.append('?');
            }
            sql.append(')');
            params.addAll(compatibleGroups);
        }

        sql.append(" ORDER BY d.available DESC, d.city ASC, u.name ASC");

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql.toString())) {
            if (connection == null || statement == null) {
                return donors;
            }

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    donors.add(mapDonor(resultSet));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return donors;
    }

    private Donor mapDonor(ResultSet resultSet) throws SQLException {
        Donor donor = new Donor();
        donor.setId(resultSet.getInt("id"));
        donor.setUserId(resultSet.getInt("user_id"));
        donor.setName(resultSet.getString("name"));
        donor.setEmail(resultSet.getString("email"));
        donor.setPhone(resultSet.getString("phone"));
        donor.setBloodGroup(resultSet.getString("blood_group"));
        donor.setLocation(resultSet.getString("location"));
        donor.setCity(resultSet.getString("city"));
        Date lastDonationDate = resultSet.getDate("last_donation_date");
        donor.setLastDonationDate(lastDonationDate == null ? null : lastDonationDate.toString());
        donor.setAvailable(resultSet.getBoolean("available"));
        return donor;
    }

    private void setOptionalDate(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            statement.setNull(index, Types.DATE);
            return;
        }
        statement.setDate(index, Date.valueOf(value.trim()));
    }
}
