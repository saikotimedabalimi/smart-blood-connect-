package com.smartblood.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;

public final class DBConnection {
    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/smart_blood_connect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASSWORD = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    private DBConnection() {
    }

    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(getConfiguredUrl(), getConfiguredUser(), getConfiguredPassword());
        } catch (SQLException ex) {
            if (isUnknownDatabase(ex) && createDatabaseIfMissing()) {
                try {
                    return DriverManager.getConnection(getConfiguredUrl(), getConfiguredUser(), getConfiguredPassword());
                } catch (SQLException retryEx) {
                    retryEx.printStackTrace();
                    return null;
                }
            }
            ex.printStackTrace();
            return null;
        }
    }

    public static String getConfiguredUrl() {
        return readSetting("SMART_BLOOD_DB_URL", "smartblood.db.url", DEFAULT_URL);
    }

    public static String getConfiguredUser() {
        return readSetting("SMART_BLOOD_DB_USER", "smartblood.db.user", DEFAULT_USER);
    }

    public static String getConfiguredPassword() {
        return readSetting("SMART_BLOOD_DB_PASSWORD", "smartblood.db.password", DEFAULT_PASSWORD);
    }

    private static boolean createDatabaseIfMissing() {
        String serverUrl = getServerUrl(getConfiguredUrl());
        String databaseName = getDatabaseName(getConfiguredUrl());
        if (serverUrl == null || databaseName == null) {
            return false;
        }

        try (Connection connection =
                     DriverManager.getConnection(serverUrl, getConfiguredUser(), getConfiguredPassword());
             Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE DATABASE IF NOT EXISTS `" + databaseName + "`");
            return true;
        } catch (SQLException ex) {
            ex.printStackTrace();
            return false;
        }
    }

    private static boolean isUnknownDatabase(SQLException ex) {
        String message = ex.getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains("unknown database");
    }

    private static String getServerUrl(String jdbcUrl) {
        if (jdbcUrl == null || !jdbcUrl.startsWith("jdbc:mysql://")) {
            return null;
        }
        int schemeEnd = jdbcUrl.indexOf("//");
        int pathStart = jdbcUrl.indexOf('/', schemeEnd + 2);
        if (pathStart < 0) {
            return jdbcUrl;
        }
        int queryStart = jdbcUrl.indexOf('?', pathStart);
        String query = queryStart >= 0 ? jdbcUrl.substring(queryStart) : "";
        return jdbcUrl.substring(0, pathStart + 1) + query;
    }

    private static String getDatabaseName(String jdbcUrl) {
        if (jdbcUrl == null) {
            return null;
        }
        int pathStart = jdbcUrl.lastIndexOf('/');
        if (pathStart < 0 || pathStart == jdbcUrl.length() - 1) {
            return null;
        }
        int queryStart = jdbcUrl.indexOf('?', pathStart);
        return queryStart >= 0 ? jdbcUrl.substring(pathStart + 1, queryStart) : jdbcUrl.substring(pathStart + 1);
    }

    private static String readSetting(String envName, String propertyName, String fallback) {
        String propertyValue = System.getProperty(propertyName);
        if (propertyValue != null && !propertyValue.trim().isEmpty()) {
            return propertyValue.trim();
        }

        String envValue = System.getenv(envName);
        if (envValue != null && !envValue.trim().isEmpty()) {
            return envValue.trim();
        }

        return fallback;
    }
}
