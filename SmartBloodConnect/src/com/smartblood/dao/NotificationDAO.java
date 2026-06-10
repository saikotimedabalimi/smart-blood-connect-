package com.smartblood.dao;

import com.smartblood.model.Notification;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class NotificationDAO {
    public void createNotification(int userId, String title, String message, String category, Integer requestId) {
        SchemaManager.ensureSchema();
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                return;
            }
            createNotification(connection, userId, title, message, category, requestId);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    void createNotification(Connection connection, int userId, String title, String message, String category, Integer requestId)
            throws SQLException {
        String sql = "INSERT INTO notifications(user_id, title, message, category, reference_request_id) VALUES(?,?,?,?,?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, title);
            statement.setString(3, message);
            statement.setString(4, category);
            if (requestId == null || requestId <= 0) {
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setInt(5, requestId);
            }
            statement.executeUpdate();
        }
    }

    public List<Notification> getLatestNotifications(int userId, int limit) {
        SchemaManager.ensureSchema();
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id=? ORDER BY id DESC LIMIT ?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return notifications;
            }
            statement.setInt(1, userId);
            statement.setInt(2, Math.max(1, limit));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapNotification(resultSet));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return notifications;
    }

    public List<Notification> getUnreadNotifications(int userId) {
        SchemaManager.ensureSchema();
        List<Notification> notifications = new ArrayList<>();
        String sql = "SELECT * FROM notifications WHERE user_id=? AND is_read = 0 ORDER BY id ASC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return notifications;
            }
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapNotification(resultSet));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return notifications;
    }

    public List<Notification> getUnreadNotificationsAndMarkRead(int userId) {
        SchemaManager.ensureSchema();
        List<Notification> notifications = new ArrayList<>();
        String selectSql = "SELECT * FROM notifications WHERE user_id=? AND is_read = 0 ORDER BY id ASC";
        String updateSql = "UPDATE notifications SET is_read = 1 WHERE user_id=? AND is_read = 0";
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                return notifications;
            }

            connection.setAutoCommit(false);
            try (PreparedStatement select = connection.prepareStatement(selectSql)) {
                select.setInt(1, userId);
                try (ResultSet resultSet = select.executeQuery()) {
                    while (resultSet.next()) {
                        notifications.add(mapNotification(resultSet));
                    }
                }
            }

            try (PreparedStatement update = connection.prepareStatement(updateSql)) {
                update.setInt(1, userId);
                update.executeUpdate();
            }

            connection.commit();
            connection.setAutoCommit(true);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return notifications;
    }

    public int getUnreadCount(int userId) {
        SchemaManager.ensureSchema();
        String sql = "SELECT COUNT(*) FROM notifications WHERE user_id=? AND is_read = 0";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return 0;
            }
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return 0;
        }
    }

    private Notification mapNotification(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification();
        notification.setId(resultSet.getInt("id"));
        notification.setUserId(resultSet.getInt("user_id"));
        notification.setTitle(resultSet.getString("title"));
        notification.setMessage(resultSet.getString("message"));
        notification.setCategory(resultSet.getString("category"));
        notification.setReferenceRequestId(resultSet.getInt("reference_request_id"));
        notification.setRead(resultSet.getBoolean("is_read"));
        Timestamp createdAt = resultSet.getTimestamp("created_at");
        notification.setCreatedAt(createdAt == null ? "" : createdAt.toLocalDateTime().toString().replace('T', ' '));
        return notification;
    }
}
