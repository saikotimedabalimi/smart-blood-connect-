package com.smartblood.dao;

import com.smartblood.model.Certificate;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CertificateDAO {
    public Certificate findByRequestAndDonor(int requestId, int donorUserId) {
        SchemaManager.ensureSchema();
        String sql = "SELECT c.*, donor.name AS donor_name, r.patient_name, r.blood_group, "
                + "COALESCE(r.hospital_name, r.location) AS hospital_name, r.city "
                + "FROM certificates c "
                + "JOIN requests r ON r.id = c.request_id "
                + "JOIN users donor ON donor.id = c.donor_user_id "
                + "WHERE c.request_id=? AND c.donor_user_id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return null;
            }
            statement.setInt(1, requestId);
            statement.setInt(2, donorUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapCertificate(resultSet) : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public Certificate findByRequestId(int requestId) {
        SchemaManager.ensureSchema();
        String sql = "SELECT c.*, donor.name AS donor_name, r.patient_name, r.blood_group, "
                + "COALESCE(r.hospital_name, r.location) AS hospital_name, r.city "
                + "FROM certificates c "
                + "JOIN requests r ON r.id = c.request_id "
                + "JOIN users donor ON donor.id = c.donor_user_id "
                + "WHERE c.request_id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return null;
            }
            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapCertificate(resultSet) : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private Certificate mapCertificate(ResultSet resultSet) throws SQLException {
        Certificate certificate = new Certificate();
        certificate.setId(resultSet.getInt("id"));
        certificate.setRequestId(resultSet.getInt("request_id"));
        certificate.setDonorUserId(resultSet.getInt("donor_user_id"));
        certificate.setRequesterUserId(resultSet.getInt("requester_user_id"));
        certificate.setCertificateNumber(resultSet.getString("certificate_number"));
        certificate.setIssuerType(resultSet.getString("issuer_type"));
        certificate.setIssuerName(resultSet.getString("issuer_name"));
        Date issuedOn = resultSet.getDate("issued_on");
        certificate.setIssuedOn(issuedOn == null ? "" : issuedOn.toString());
        certificate.setDonorName(resultSet.getString("donor_name"));
        certificate.setPatientName(resultSet.getString("patient_name"));
        certificate.setBloodGroup(resultSet.getString("blood_group"));
        certificate.setHospitalName(resultSet.getString("hospital_name"));
        certificate.setCity(resultSet.getString("city"));
        return certificate;
    }
}
