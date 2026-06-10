package com.smartblood.dao;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public final class SchemaManager {
    private static volatile boolean initialized;

    private SchemaManager() {
    }

    public static synchronized void ensureSchema() {
        if (initialized) {
            return;
        }

        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                return;
            }

            createBaseTables(connection);
            upgradeExistingTables(connection);
            seedLookupData(connection);
            initialized = true;
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private static void createBaseTables(Connection connection) throws SQLException {
        execute(connection, "CREATE TABLE IF NOT EXISTS blood_groups ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "group_name VARCHAR(5) NOT NULL UNIQUE"
                + ")");

        execute(connection, "CREATE TABLE IF NOT EXISTS users ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "name VARCHAR(100) NOT NULL,"
                + "email VARCHAR(120) NOT NULL UNIQUE,"
                + "password VARCHAR(100) NOT NULL,"
                + "phone VARCHAR(20) NOT NULL,"
                + "city VARCHAR(100) NOT NULL,"
                + "location VARCHAR(150) NOT NULL,"
                + "account_type VARCHAR(20) NOT NULL,"
                + "role VARCHAR(20) NOT NULL DEFAULT 'USER',"
                + "notification_enabled TINYINT(1) NOT NULL DEFAULT 1,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ")");

        execute(connection, "CREATE TABLE IF NOT EXISTS donors ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "user_id INT NOT NULL UNIQUE,"
                + "blood_group VARCHAR(5) NOT NULL,"
                + "location VARCHAR(150) NOT NULL,"
                + "city VARCHAR(100) NOT NULL,"
                + "last_donation_date DATE NULL,"
                + "available TINYINT(1) NOT NULL DEFAULT 1,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ")");

        execute(connection, "CREATE TABLE IF NOT EXISTS requests ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "requester_user_id INT NOT NULL,"
                + "patient_name VARCHAR(100) NOT NULL,"
                + "blood_group VARCHAR(5) NOT NULL,"
                + "location VARCHAR(150) NOT NULL,"
                + "city VARCHAR(100) NOT NULL,"
                + "reason TEXT NOT NULL,"
                + "units_required INT NOT NULL DEFAULT 1,"
                + "emergency TINYINT(1) NOT NULL DEFAULT 0,"
                + "status VARCHAR(40) NOT NULL DEFAULT 'Open for Matching',"
                + "matched_donor_summary VARCHAR(255),"
                + "assigned_donor_user_id INT NULL,"
                + "contact_name VARCHAR(100) NULL,"
                + "contact_phone VARCHAR(20) NULL,"
                + "hospital_name VARCHAR(150) NULL,"
                + "required_by DATETIME NULL,"
                + "formality_notes TEXT NULL,"
                + "procurement_status VARCHAR(40) NOT NULL DEFAULT 'Awaiting donor response',"
                + "completed_at TIMESTAMP NULL DEFAULT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (requester_user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ")");

        execute(connection, "CREATE TABLE IF NOT EXISTS donor_request_targets ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "request_id INT NOT NULL,"
                + "donor_user_id INT NOT NULL,"
                + "response_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',"
                + "response_note VARCHAR(255) NULL,"
                + "responded_at TIMESTAMP NULL DEFAULT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "UNIQUE KEY uq_request_donor (request_id, donor_user_id),"
                + "FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (donor_user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ")");

        execute(connection, "CREATE TABLE IF NOT EXISTS notifications ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "user_id INT NOT NULL,"
                + "title VARCHAR(120) NOT NULL,"
                + "message VARCHAR(255) NOT NULL,"
                + "category VARCHAR(40) NOT NULL,"
                + "reference_request_id INT NULL,"
                + "is_read TINYINT(1) NOT NULL DEFAULT 0,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (reference_request_id) REFERENCES requests(id) ON DELETE SET NULL"
                + ")");

        execute(connection, "CREATE TABLE IF NOT EXISTS certificates ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "request_id INT NOT NULL UNIQUE,"
                + "donor_user_id INT NOT NULL,"
                + "requester_user_id INT NOT NULL,"
                + "certificate_number VARCHAR(60) NOT NULL UNIQUE,"
                + "issuer_type VARCHAR(30) NOT NULL,"
                + "issuer_name VARCHAR(150) NOT NULL,"
                + "issued_on DATE NOT NULL,"
                + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,"
                + "FOREIGN KEY (request_id) REFERENCES requests(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (donor_user_id) REFERENCES users(id) ON DELETE CASCADE,"
                + "FOREIGN KEY (requester_user_id) REFERENCES users(id) ON DELETE CASCADE"
                + ")");

        execute(connection, "CREATE TABLE IF NOT EXISTS admin ("
                + "id INT PRIMARY KEY AUTO_INCREMENT,"
                + "username VARCHAR(100) NOT NULL UNIQUE,"
                + "password VARCHAR(100) NOT NULL"
                + ")");
    }

    private static void upgradeExistingTables(Connection connection) throws SQLException {
        ensureColumn(connection, "users", "notification_enabled", "TINYINT(1) NOT NULL DEFAULT 1");
        ensureColumn(connection, "requests", "assigned_donor_user_id", "INT NULL");
        ensureColumn(connection, "requests", "contact_name", "VARCHAR(100) NULL");
        ensureColumn(connection, "requests", "contact_phone", "VARCHAR(20) NULL");
        ensureColumn(connection, "requests", "hospital_name", "VARCHAR(150) NULL");
        ensureColumn(connection, "requests", "required_by", "DATETIME NULL");
        ensureColumn(connection, "requests", "formality_notes", "TEXT NULL");
        ensureColumn(connection, "requests", "procurement_status", "VARCHAR(40) NOT NULL DEFAULT 'Awaiting donor response'");
        ensureColumn(connection, "requests", "completed_at", "TIMESTAMP NULL DEFAULT NULL");

        execute(connection, "UPDATE users SET notification_enabled = 1 WHERE notification_enabled IS NULL");
        execute(connection, "UPDATE requests SET procurement_status = "
                + "CASE "
                + "WHEN status = 'Donation Completed' THEN 'Completed' "
                + "WHEN status = 'Accepted by Donor' THEN 'Awaiting requester details' "
                + "WHEN status = 'Rejected by Donor' THEN 'Closed' "
                + "ELSE 'Awaiting donor response' "
                + "END "
                + "WHERE procurement_status IS NULL OR procurement_status = ''");
    }

    private static void seedLookupData(Connection connection) throws SQLException {
        String[] groups = {"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"};
        for (String group : groups) {
            try (PreparedStatement statement =
                         connection.prepareStatement("INSERT IGNORE INTO blood_groups(group_name) VALUES(?)")) {
                statement.setString(1, group);
                statement.executeUpdate();
            }
        }

        try (PreparedStatement statement =
                     connection.prepareStatement("INSERT IGNORE INTO admin(username, password) VALUES(?, ?)")) {
            statement.setString(1, "admin@smartblood.com");
            statement.setString(2, "admin123");
            statement.executeUpdate();
        }
    }

    private static void ensureColumn(Connection connection, String table, String column, String definition)
            throws SQLException {
        if (columnExists(connection, table, column)) {
            return;
        }
        execute(connection, "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
    }

    private static boolean columnExists(Connection connection, String table, String column) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        try (ResultSet resultSet = metaData.getColumns(connection.getCatalog(), null, table, column)) {
            return resultSet.next();
        }
    }

    private static void execute(Connection connection, String sql) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }
    }
}
