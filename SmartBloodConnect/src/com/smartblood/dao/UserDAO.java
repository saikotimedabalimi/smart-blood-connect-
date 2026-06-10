package com.smartblood.dao;

import com.smartblood.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {
    public int registerUser(User user) throws Exception {
        SchemaManager.ensureSchema();
        String sql = "INSERT INTO users(name,email,password,phone,city,location,account_type,role,notification_enabled) "
                + "VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null
                     : connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            if (connection == null || statement == null) {
                return -1;
            }

            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPhone());
            statement.setString(5, user.getCity());
            statement.setString(6, user.getLocation());
            statement.setString(7, user.getAccountType());
            statement.setString(8, user.getRole());
            statement.setBoolean(9, user.isNotificationEnabled());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
        }
        return -1;
    }

    public boolean emailExists(String email) {
        SchemaManager.ensureSchema();
        String sql = "SELECT id FROM users WHERE email=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return true;
            }
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return true;
        }
    }

    public User authenticateUser(String email, String password) {
        SchemaManager.ensureSchema();
        String sql = "SELECT * FROM users WHERE email=? AND password=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return null;
            }
            statement.setString(1, email);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public boolean authenticateAdmin(String username, String password) {
        SchemaManager.ensureSchema();
        String sql = "SELECT id FROM admin WHERE username=? AND password=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return false;
            }
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
    }

    public List<User> getAllUsers() {
        SchemaManager.ensureSchema();
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users ORDER BY id DESC";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return users;
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    users.add(mapUser(resultSet));
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return users;
    }

    public User findById(int id) {
        SchemaManager.ensureSchema();
        String sql = "SELECT * FROM users WHERE id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return null;
            }
            statement.setInt(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapUser(resultSet) : null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public void updateProfile(User user) {
        SchemaManager.ensureSchema();
        boolean updatePassword = user.getPassword() != null && !user.getPassword().isEmpty();
        String sql = updatePassword 
            ? "UPDATE users SET name=?, phone=?, city=?, location=?, account_type=?, notification_enabled=?, password=? WHERE id=?"
            : "UPDATE users SET name=?, phone=?, city=?, location=?, account_type=?, notification_enabled=? WHERE id=?";
        
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return;
            }
            statement.setString(1, user.getName());
            statement.setString(2, user.getPhone());
            statement.setString(3, user.getCity());
            statement.setString(4, user.getLocation());
            statement.setString(5, user.getAccountType());
            statement.setBoolean(6, user.isNotificationEnabled());
            if (updatePassword) {
                statement.setString(7, user.getPassword());
                statement.setInt(8, user.getId());
            } else {
                statement.setInt(7, user.getId());
            }
            statement.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void deleteUser(int id) {
        SchemaManager.ensureSchema();
        String sql = "DELETE FROM users WHERE id=?";
        try (Connection connection = DBConnection.getConnection();
             PreparedStatement statement = connection == null ? null : connection.prepareStatement(sql)) {
            if (connection == null || statement == null) {
                return;
            }
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setId(resultSet.getInt("id"));
        user.setName(resultSet.getString("name"));
        user.setEmail(resultSet.getString("email"));
        user.setPassword(resultSet.getString("password"));
        user.setPhone(resultSet.getString("phone"));
        user.setCity(resultSet.getString("city"));
        user.setLocation(resultSet.getString("location"));
        user.setAccountType(resultSet.getString("account_type"));
        user.setRole(resultSet.getString("role"));
        user.setNotificationEnabled(resultSet.getBoolean("notification_enabled"));
        return user;
    }
}
