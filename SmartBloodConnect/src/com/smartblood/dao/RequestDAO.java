package com.smartblood.dao;

import com.smartblood.model.Certificate;
import com.smartblood.model.Donor;
import com.smartblood.model.Request;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class RequestDAO {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public boolean createRequest(Request request) {
        SchemaManager.ensureSchema();
        DonorDAO donorDAO = new DonorDAO();
        List<Donor> targets =
                donorDAO.getTargetDonors(request.getBloodGroup(), request.getCity(), request.getSelectedDonorUserId(), true);

        if (request.getSelectedDonorUserId() > 0 && targets.isEmpty()) {
            return false;
        }

        String status = request.getSelectedDonorUserId() > 0
                ? "Pending Donor Response"
                : (targets.isEmpty() ? "Waiting for Admin Support" : "Open for Matching");
        String summary = buildTargetSummary(request, targets);

        String sql = "INSERT INTO requests("
                + "requester_user_id, patient_name, blood_group, location, city, reason, units_required, emergency, "
                + "status, matched_donor_summary, assigned_donor_user_id, contact_name, contact_phone, hospital_name, "
                + "required_by, formality_notes, procurement_status"
                + ") VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                return false;
            }

            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, request.getRequesterUserId());
                statement.setString(2, request.getPatientName());
                statement.setString(3, request.getBloodGroup());
                statement.setString(4, request.getLocation());
                statement.setString(5, request.getCity());
                statement.setString(6, request.getReason());
                statement.setInt(7, request.getUnitsRequired());
                statement.setBoolean(8, request.isEmergency());
                statement.setString(9, status);
                statement.setString(10, summary);
                statement.setNull(11, Types.INTEGER);
                setNullableString(statement, 12, request.getContactName());
                setNullableString(statement, 13, request.getContactPhone());
                setNullableString(statement, 14, request.getHospitalName());
                setNullableDateTime(statement, 15, request.getRequiredBy());
                setNullableString(statement, 16, request.getFormalityNotes());
                statement.setString(17, "Awaiting donor response");
                statement.executeUpdate();

                int requestId = -1;
                try (ResultSet keys = statement.getGeneratedKeys()) {
                    if (keys.next()) {
                        requestId = keys.getInt(1);
                    }
                }
                if (requestId <= 0) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }

                insertTargets(connection, requestId, request, targets);
                notifyTargets(connection, requestId, request, targets);

                connection.commit();
                connection.setAutoCommit(true);
                return true;
            } catch (Exception ex) {
                connection.rollback();
                connection.setAutoCommit(true);
                ex.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<Request> getAllRequests() {
        SchemaManager.ensureSchema();
        String sql = baseSelect()
                + " ORDER BY CASE WHEN r.emergency = 1 AND r.status <> 'Donation Completed' THEN 0 ELSE 1 END, r.id DESC";
        return queryRequests(sql, null);
    }

    public List<Request> getRequestsByUser(int requesterUserId) {
        SchemaManager.ensureSchema();
        String sql = baseSelect() + " WHERE r.requester_user_id=? ORDER BY r.id DESC";
        return queryRequests(sql, requesterUserId);
    }

    public List<Request> getRequestsForDonor(int donorUserId) {
        SchemaManager.ensureSchema();
        List<Request> requests = new ArrayList<>();
        String sql = "SELECT r.*, requester.name AS requester_name, donor.name AS assigned_donor_name, "
                + "donor.email AS assigned_donor_email, donor.phone AS assigned_donor_phone, "
                + "c.certificate_number, t.response_status AS donor_response_status, t.response_note AS donor_response_note "
                + "FROM donor_request_targets t "
                + "JOIN requests r ON r.id = t.request_id "
                + "JOIN users requester ON requester.id = r.requester_user_id "
                + "LEFT JOIN users donor ON donor.id = r.assigned_donor_user_id "
                + "LEFT JOIN certificates c ON c.request_id = r.id "
                + "WHERE t.donor_user_id=? "
                + "ORDER BY CASE WHEN t.response_status = 'PENDING' THEN 0 "
                + "WHEN t.response_status = 'ACCEPTED' THEN 1 ELSE 2 END, r.emergency DESC, r.id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return requests;
            }
            statement.setInt(1, donorUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRequest(resultSet));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return requests;
    }

    public Request getRequestById(int requestId) {
        SchemaManager.ensureSchema();
        String sql = baseSelect() + " WHERE r.id=?";
        List<Request> requests = queryRequests(sql, requestId);
        return requests.isEmpty() ? null : requests.get(0);
    }

    public boolean updateRequestStatus(int requestId, String status) {
        SchemaManager.ensureSchema();
        String sql = "UPDATE requests SET status=? WHERE id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return false;
            }
            statement.setString(1, status);
            statement.setInt(2, requestId);
            return statement.executeUpdate() > 0;
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean respondToRequest(int requestId, int donorUserId, String action, String note) {
        SchemaManager.ensureSchema();
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                return false;
            }

            connection.setAutoCommit(false);
            try {
                String currentTargetStatus = getTargetStatusForUpdate(connection, requestId, donorUserId);
                if (currentTargetStatus == null || !"PENDING".equalsIgnoreCase(currentTargetStatus)) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }

                Request request = getRequestForWorkflow(connection, requestId);
                if (request == null || "Donation Completed".equalsIgnoreCase(request.getStatus())) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }

                NotificationDAO notificationDAO = new NotificationDAO();
                Donor donor = getDonorInfo(connection, donorUserId);
                if (donor == null) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }

                if ("ACCEPT".equalsIgnoreCase(action)) {
                    updateTarget(connection, requestId, donorUserId, "ACCEPTED", note);
                    expireOtherTargets(connection, requestId, donorUserId);

                    String summary = donor.getName() + " (" + donor.getPhone() + ")";
                    String procurementStatus = hasProcurementDetails(request)
                            ? "Details Shared With Donor"
                            : "Awaiting requester details";
                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE requests SET status='Accepted by Donor', assigned_donor_user_id=?, "
                                    + "matched_donor_summary=?, procurement_status=? WHERE id=?")) {
                        statement.setInt(1, donorUserId);
                        statement.setString(2, summary);
                        statement.setString(3, procurementStatus);
                        statement.setInt(4, requestId);
                        statement.executeUpdate();
                    }

                    notificationDAO.createNotification(
                            connection,
                            request.getRequesterUserId(),
                            "Donor accepted your request",
                            donor.getName() + " accepted the blood request for " + request.getPatientName() + ".",
                            "REQUEST_ACCEPTED",
                            requestId);
                } else {
                    updateTarget(connection, requestId, donorUserId, "REJECTED", note);
                    WorkflowCounts counts = getWorkflowCounts(connection, requestId);
                    String nextStatus;
                    String nextProcurementStatus;
                    if (counts.pendingCount > 0) {
                        nextStatus = "Open for Matching";
                        nextProcurementStatus = "Awaiting donor response";
                    } else if (counts.totalCount <= 1) {
                        nextStatus = "Rejected by Donor";
                        nextProcurementStatus = "Closed";
                    } else {
                        nextStatus = "No Donor Accepted";
                        nextProcurementStatus = "Closed";
                    }

                    try (PreparedStatement statement = connection.prepareStatement(
                            "UPDATE requests SET status=?, procurement_status=?, assigned_donor_user_id=NULL WHERE id=?")) {
                        statement.setString(1, nextStatus);
                        statement.setString(2, nextProcurementStatus);
                        statement.setInt(3, requestId);
                        statement.executeUpdate();
                    }

                    String message = counts.pendingCount > 0
                            ? donor.getName() + " declined the request, but it is still visible to other matching donors."
                            : donor.getName() + " declined the request and no donor has accepted it yet.";
                    notificationDAO.createNotification(
                            connection,
                            request.getRequesterUserId(),
                            "Donor declined the request",
                            message,
                            "REQUEST_REJECTED",
                            requestId);
                }

                connection.commit();
                connection.setAutoCommit(true);
                return true;
            } catch (Exception ex) {
                connection.rollback();
                connection.setAutoCommit(true);
                ex.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean updateProcurementDetails(
            int requestId,
            int requesterUserId,
            String contactName,
            String contactPhone,
            String hospitalName,
            String requiredBy,
            String formalityNotes) {

        SchemaManager.ensureSchema();
        String sql = "UPDATE requests SET contact_name=?, contact_phone=?, hospital_name=?, required_by=?, "
                + "formality_notes=?, procurement_status='Details Shared With Donor' "
                + "WHERE id=? AND requester_user_id=? AND status='Accepted by Donor'";
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                return false;
            }

            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, contactName);
                statement.setString(2, contactPhone);
                statement.setString(3, hospitalName);
                setNullableDateTime(statement, 4, requiredBy);
                statement.setString(5, formalityNotes);
                statement.setInt(6, requestId);
                statement.setInt(7, requesterUserId);

                int updated = statement.executeUpdate();
                if (updated <= 0) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }

                Request request = getRequestForWorkflow(connection, requestId);
                if (request != null && request.getAssignedDonorUserId() > 0) {
                    new NotificationDAO().createNotification(
                            connection,
                            request.getAssignedDonorUserId(),
                            "Contact details shared",
                            "Requester shared hospital and contact details for request #" + requestId + ".",
                            "FORMALITIES_SHARED",
                            requestId);
                }

                connection.commit();
                connection.setAutoCommit(true);
                return true;
            } catch (Exception ex) {
                connection.rollback();
                connection.setAutoCommit(true);
                ex.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public boolean completeDonation(
            int requestId,
            String issuerType,
            String issuerName,
            String certificateNumber,
            String issuedOn) {

        SchemaManager.ensureSchema();
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                return false;
            }

            connection.setAutoCommit(false);
            try {
                Request request = getRequestForWorkflow(connection, requestId);
                if (request == null || request.getAssignedDonorUserId() <= 0) {
                    connection.rollback();
                    connection.setAutoCommit(true);
                    return false;
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE requests SET status='Donation Completed', procurement_status='Completed', completed_at=NOW() "
                                + "WHERE id=?")) {
                    statement.setInt(1, requestId);
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO certificates(request_id, donor_user_id, requester_user_id, certificate_number, issuer_type, issuer_name, issued_on) "
                                + "VALUES(?,?,?,?,?,?,?) "
                                + "ON DUPLICATE KEY UPDATE certificate_number=VALUES(certificate_number), "
                                + "issuer_type=VALUES(issuer_type), issuer_name=VALUES(issuer_name), issued_on=VALUES(issued_on)")) {
                    statement.setInt(1, requestId);
                    statement.setInt(2, request.getAssignedDonorUserId());
                    statement.setInt(3, request.getRequesterUserId());
                    statement.setString(4, certificateNumber);
                    statement.setString(5, issuerType);
                    statement.setString(6, issuerName);
                    statement.setDate(7, Date.valueOf(LocalDate.parse(issuedOn)));
                    statement.executeUpdate();
                }

                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE donors SET available=0, last_donation_date=? WHERE user_id=?")) {
                    statement.setDate(1, Date.valueOf(LocalDate.parse(issuedOn)));
                    statement.setInt(2, request.getAssignedDonorUserId());
                    statement.executeUpdate();
                }

                NotificationDAO notificationDAO = new NotificationDAO();
                notificationDAO.createNotification(
                        connection,
                        request.getAssignedDonorUserId(),
                        "Certificate issued",
                        "Your donation certificate for request #" + requestId + " is ready to download.",
                        "CERTIFICATE_READY",
                        requestId);
                notificationDAO.createNotification(
                        connection,
                        request.getRequesterUserId(),
                        "Donation marked complete",
                        "Donation for " + request.getPatientName() + " has been marked complete by admin.",
                        "DONATION_COMPLETED",
                        requestId);

                connection.commit();
                connection.setAutoCommit(true);
                return true;
            } catch (Exception ex) {
                connection.rollback();
                connection.setAutoCommit(true);
                ex.printStackTrace();
                return false;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public Certificate getCertificateForDonor(int requestId, int donorUserId) {
        return new CertificateDAO().findByRequestAndDonor(requestId, donorUserId);
    }

    private List<Request> queryRequests(String sql, Integer idParam) {
        List<Request> requests = new ArrayList<>();
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return requests;
            }
            if (idParam != null) {
                statement.setInt(1, idParam);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    requests.add(mapRequest(resultSet));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return requests;
    }

    private String baseSelect() {
        return "SELECT r.*, requester.name AS requester_name, donor.name AS assigned_donor_name, "
                + "donor.email AS assigned_donor_email, donor.phone AS assigned_donor_phone, "
                + "c.certificate_number "
                + "FROM requests r "
                + "JOIN users requester ON requester.id = r.requester_user_id "
                + "LEFT JOIN users donor ON donor.id = r.assigned_donor_user_id "
                + "LEFT JOIN certificates c ON c.request_id = r.id";
    }

    private void insertTargets(Connection connection, int requestId, Request request, List<Donor> targets) throws SQLException {
        if (targets.isEmpty()) {
            return;
        }

        String sql = "INSERT INTO donor_request_targets(request_id, donor_user_id, response_status) VALUES(?,?,'PENDING')";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            for (Donor donor : targets) {
                statement.setInt(1, requestId);
                statement.setInt(2, donor.getUserId());
                statement.addBatch();
            }
            statement.executeBatch();
        }
    }

    private void notifyTargets(Connection connection, int requestId, Request request, List<Donor> targets) throws SQLException {
        NotificationDAO notificationDAO = new NotificationDAO();
        for (Donor donor : targets) {
            String title = request.getSelectedDonorUserId() > 0 ? "Direct blood request" : "Blood request nearby";
            String message = request.getRequesterName() + " needs " + request.getBloodGroup()
                    + " blood for " + request.getPatientName() + " in " + request.getCity() + ".";
            notificationDAO.createNotification(connection, donor.getUserId(), title, message, "REQUEST_CREATED", requestId);
        }
    }

    private String buildTargetSummary(Request request, List<Donor> targets) {
        if (request.getSelectedDonorUserId() > 0 && !targets.isEmpty()) {
            Donor donor = targets.get(0);
            return "Direct request sent to " + donor.getName() + " (" + donor.getPhone() + ")";
        }
        if (!targets.isEmpty()) {
            return "Broadcast sent to " + targets.size() + " compatible donor(s).";
        }
        return "No compatible donor is currently available in this city.";
    }

    private String getTargetStatusForUpdate(Connection connection, int requestId, int donorUserId) throws SQLException {
        String sql = "SELECT response_status FROM donor_request_targets WHERE request_id=? AND donor_user_id=? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.setInt(2, donorUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getString("response_status") : null;
            }
        }
    }

    private void updateTarget(Connection connection, int requestId, int donorUserId, String status, String note) throws SQLException {
        String sql = "UPDATE donor_request_targets SET response_status=?, response_note=?, responded_at=NOW() "
                + "WHERE request_id=? AND donor_user_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            setNullableString(statement, 2, note);
            statement.setInt(3, requestId);
            statement.setInt(4, donorUserId);
            statement.executeUpdate();
        }
    }

    private void expireOtherTargets(Connection connection, int requestId, int acceptedDonorUserId) throws SQLException {
        String sql = "UPDATE donor_request_targets SET response_status='EXPIRED', response_note='Another donor accepted first.', "
                + "responded_at=NOW() WHERE request_id=? AND donor_user_id<>? AND response_status='PENDING'";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            statement.setInt(2, acceptedDonorUserId);
            statement.executeUpdate();
        }
    }

    private WorkflowCounts getWorkflowCounts(Connection connection, int requestId) throws SQLException {
        WorkflowCounts counts = new WorkflowCounts();
        String sql = "SELECT "
                + "COUNT(*) AS total_count, "
                + "SUM(CASE WHEN response_status='PENDING' THEN 1 ELSE 0 END) AS pending_count "
                + "FROM donor_request_targets WHERE request_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    counts.totalCount = resultSet.getInt("total_count");
                    counts.pendingCount = resultSet.getInt("pending_count");
                }
            }
        }
        return counts;
    }

    private Request getRequestForWorkflow(Connection connection, int requestId) throws SQLException {
        String sql = "SELECT * FROM requests WHERE id=? FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, requestId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Request request = new Request();
                request.setId(resultSet.getInt("id"));
                request.setRequesterUserId(resultSet.getInt("requester_user_id"));
                request.setPatientName(resultSet.getString("patient_name"));
                request.setBloodGroup(resultSet.getString("blood_group"));
                request.setCity(resultSet.getString("city"));
                request.setStatus(resultSet.getString("status"));
                request.setAssignedDonorUserId(resultSet.getInt("assigned_donor_user_id"));
                request.setContactName(resultSet.getString("contact_name"));
                request.setContactPhone(resultSet.getString("contact_phone"));
                request.setHospitalName(resultSet.getString("hospital_name"));
                Timestamp requiredBy = resultSet.getTimestamp("required_by");
                request.setRequiredBy(requiredBy == null ? null : formatTimestamp(requiredBy, true));
                request.setFormalityNotes(resultSet.getString("formality_notes"));
                return request;
            }
        }
    }

    private Donor getDonorInfo(Connection connection, int donorUserId) throws SQLException {
        String sql = "SELECT d.*, u.name, u.email, u.phone FROM donors d JOIN users u ON u.id=d.user_id WHERE d.user_id=?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, donorUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Donor donor = new Donor();
                donor.setId(resultSet.getInt("id"));
                donor.setUserId(resultSet.getInt("user_id"));
                donor.setName(resultSet.getString("name"));
                donor.setEmail(resultSet.getString("email"));
                donor.setPhone(resultSet.getString("phone"));
                donor.setBloodGroup(resultSet.getString("blood_group"));
                donor.setCity(resultSet.getString("city"));
                donor.setLocation(resultSet.getString("location"));
                Date lastDonation = resultSet.getDate("last_donation_date");
                donor.setLastDonationDate(lastDonation == null ? null : lastDonation.toString());
                donor.setAvailable(resultSet.getBoolean("available"));
                return donor;
            }
        }
    }

    private Request mapRequest(ResultSet resultSet) throws SQLException {
        Request request = new Request();
        request.setId(resultSet.getInt("id"));
        request.setRequesterUserId(resultSet.getInt("requester_user_id"));
        request.setRequesterName(getString(resultSet, "requester_name"));
        request.setPatientName(getString(resultSet, "patient_name"));
        request.setBloodGroup(getString(resultSet, "blood_group"));
        request.setLocation(getString(resultSet, "location"));
        request.setCity(getString(resultSet, "city"));
        request.setReason(getString(resultSet, "reason"));
        request.setUnitsRequired(resultSet.getInt("units_required"));
        request.setEmergency(resultSet.getBoolean("emergency"));
        request.setStatus(getString(resultSet, "status"));
        request.setMatchedDonorSummary(getString(resultSet, "matched_donor_summary"));
        request.setAssignedDonorUserId(resultSet.getInt("assigned_donor_user_id"));
        request.setAssignedDonorName(getString(resultSet, "assigned_donor_name"));
        request.setAssignedDonorEmail(getString(resultSet, "assigned_donor_email"));
        request.setAssignedDonorPhone(getString(resultSet, "assigned_donor_phone"));
        request.setContactName(getString(resultSet, "contact_name"));
        request.setContactPhone(getString(resultSet, "contact_phone"));
        request.setHospitalName(getString(resultSet, "hospital_name"));
        request.setFormalityNotes(getString(resultSet, "formality_notes"));
        request.setProcurementStatus(getString(resultSet, "procurement_status"));
        request.setCreatedAt(formatTimestamp(resultSet.getTimestamp("created_at"), false));
        request.setCompletedAt(formatTimestamp(resultSet.getTimestamp("completed_at"), false));
        request.setRequiredBy(formatTimestamp(resultSet.getTimestamp("required_by"), true));
        if (hasColumn(resultSet, "donor_response_status")) {
            request.setDonorResponseStatus(getString(resultSet, "donor_response_status"));
        }
        if (hasColumn(resultSet, "donor_response_note")) {
            request.setDonorResponseNote(getString(resultSet, "donor_response_note"));
        }
        if (hasColumn(resultSet, "certificate_number")) {
            String certificateNumber = getString(resultSet, "certificate_number");
            request.setCertificateNumber(certificateNumber);
            request.setCertificateAvailable(certificateNumber != null && !certificateNumber.isEmpty());
        }
        request.setTargetedRequest(request.getMatchedDonorSummary() != null
                && request.getMatchedDonorSummary().startsWith("Direct request"));
        return request;
    }

    private boolean hasProcurementDetails(Request request) {
        return isFilled(request.getContactName())
                || isFilled(request.getContactPhone())
                || isFilled(request.getHospitalName())
                || isFilled(request.getRequiredBy())
                || isFilled(request.getFormalityNotes());
    }

    private boolean isFilled(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String getString(ResultSet resultSet, String column) throws SQLException {
        return hasColumn(resultSet, column) ? resultSet.getString(column) : null;
    }

    private boolean hasColumn(ResultSet resultSet, String column) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        for (int i = 1; i <= metaData.getColumnCount(); i++) {
            if (column.equalsIgnoreCase(metaData.getColumnLabel(i)) || column.equalsIgnoreCase(metaData.getColumnName(i))) {
                return true;
            }
        }
        return false;
    }

    private String formatTimestamp(Timestamp timestamp, boolean forInput) {
        if (timestamp == null) {
            return null;
        }
        LocalDateTime value = timestamp.toLocalDateTime();
        return value.format(forInput ? INPUT_FORMAT : DISPLAY_FORMAT);
    }

    private void setNullableString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            statement.setNull(index, Types.VARCHAR);
            return;
        }
        statement.setString(index, value.trim());
    }

    private void setNullableDateTime(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) {
            statement.setNull(index, Types.TIMESTAMP);
            return;
        }
        statement.setTimestamp(index, Timestamp.valueOf(LocalDateTime.parse(value.trim())));
    }

    private static final class WorkflowCounts {
        private int totalCount;
        private int pendingCount;
    }
}
