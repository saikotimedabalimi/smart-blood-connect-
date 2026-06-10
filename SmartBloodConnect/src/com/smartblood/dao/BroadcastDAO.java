package com.smartblood.dao;

import com.smartblood.model.Broadcast;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class BroadcastDAO {
    public void createBroadcast(Broadcast broadcast) throws Exception {
        SchemaManager.ensureSchema();
        String sql = "INSERT INTO global_broadcasts(user_id, message, blood_group, city) VALUES(?,?,?,?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return;
            }
            statement.setInt(1, broadcast.getUserId());
            statement.setString(2, broadcast.getMessage());
            statement.setString(3, broadcast.getBloodGroup());
            statement.setString(4, broadcast.getCity());
            statement.executeUpdate();
        }
    }

    public List<Broadcast> getAllActiveBroadcasts() {
        SchemaManager.ensureSchema();
        List<Broadcast> broadcasts = new ArrayList<>();
        // Fetch last 24 hours broadcasts for "active" or just all recent ones
        String sql = "SELECT b.*, u.name as user_name FROM global_broadcasts b "
                + "JOIN users u ON u.id = b.user_id "
                + "ORDER BY b.created_at DESC LIMIT 20";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return broadcasts;
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Broadcast b = new Broadcast();
                    b.setId(resultSet.getInt("id"));
                    b.setUserId(resultSet.getInt("user_id"));
                    b.setUserName(resultSet.getString("user_name"));
                    b.setMessage(resultSet.getString("message"));
                    b.setBloodGroup(resultSet.getString("blood_group"));
                    b.setCity(resultSet.getString("city"));
                    b.setCreatedAt(resultSet.getTimestamp("created_at"));
                    broadcasts.add(b);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return broadcasts;
    }
}
