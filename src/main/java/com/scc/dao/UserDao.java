
package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class UserDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    public boolean register(
            String username,
            String password,
            String fullName,
            String role) {

        String sql = """
            INSERT INTO USERS
                (username, password, full_name, role, status)
            VALUES (?, ?, ?, ?, 'PENDING')
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);
            ps.setString(2, password);
            ps.setString(3, fullName);
            ps.setString(4, role.toUpperCase(Locale.ROOT));

            ps.executeUpdate();

            System.out.println("Registration successful!");
            System.out.println("Account status: PENDING");
            System.out.println(
                    "Please wait for an administrator to approve your account."
            );

            return true;

        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {
                System.out.println(
                        "Registration failed: Username already exists."
                );
            } else {
                System.out.println(
                        "Registration failed: " + e.getMessage()
                );
            }

            return false;
        }
    }

    public User login(String username, String password) {

        String sql = """
            SELECT user_id, username, password,
                   full_name, role, status
            FROM USERS
            WHERE username = ?
              AND status IN ('APPROVED', 'Active')
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    String storedPassword = rs.getString("password");

                    boolean matches;

                    if (storedPassword != null
                            && storedPassword.matches("[0-9a-fA-F]{64}")) {
                        matches = PasswordUtil.verifyPassword(
                                password, storedPassword
                        );
                    } else {
                        matches = password.equals(storedPassword);
                    }

                    if (matches) {
                        return mapUser(rs);
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }

    public List<User> getAllUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
            SELECT user_id, username, full_name, role, status
            FROM USERS
            ORDER BY user_id
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error retrieving users: " + e.getMessage()
            );
        }

        return users;
    }

    public List<User> getPendingUsers() {

        List<User> users = new ArrayList<>();

        String sql = """
            SELECT user_id, username, full_name, role, status
            FROM USERS
            WHERE status = 'PENDING'
            ORDER BY user_id
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                users.add(mapUser(rs));
            }

        } catch (SQLException e) {
            System.out.println(
                    "Error retrieving pending users: " + e.getMessage()
            );
        }

        return users;
    }

    // DELIVERABLE 5 AND 8: APPROVE USER WITH ACTIVITY LOG

    public boolean approveUser(int userId, int adminId) {

        String sql = """
            UPDATE USERS
            SET status = 'APPROVED'
            WHERE user_id = ?
              AND status = 'PENDING'
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setInt(1, userId);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated == 1) {

                recordActivity(
                        adminId,
                        "USER_APPROVED",
                        "Admin [ID " + adminId
                                + "] approved account for User ID " + userId
                );

                return true;
            }

            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Error approving user: " + e.getMessage()
            );
            return false;
        }
    }

    // DELIVERABLE 7 AND 8: UPDATE ROLE/STATUS WITH ACTIVITY LOG

    public boolean updateUserRoleAndStatus(
            int userId,
            String newRole,
            String newStatus,
            int adminId) {

        String role = newRole.toUpperCase(Locale.ROOT);
        String status = newStatus.toUpperCase(Locale.ROOT);

        String sql = """
            UPDATE USERS
            SET role = ?, status = ?
            WHERE user_id = ?
            """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, role);
            ps.setString(2, status);
            ps.setInt(3, userId);

            int rowsUpdated = ps.executeUpdate();

            if (rowsUpdated == 1) {

                recordActivity(
                        adminId,
                        "USER_UPDATED",
                        "Admin [ID " + adminId
                                + "] changed role of User " + userId
                                + " to " + role
                                + " and status to " + status
                );

                return true;
            }

            return false;

        } catch (SQLException e) {
            System.out.println(
                    "Error updating user: " + e.getMessage()
            );
            return false;
        }
    }

    // SAVE ACTIVITY TO DATABASE

    private void recordActivity(
            int adminId,
            String actionType,
            String description) {

        try {
            activityLogDao.logAction(
                    adminId,
                    actionType,
                    description
            );

        } catch (SQLException e) {
            System.out.println(
                    "WARNING: User action succeeded, but activity logging failed: "
                            + e.getMessage()
            );
        }
    }

    private User mapUser(ResultSet rs) throws SQLException {

        return new User(
                rs.getInt("user_id"),
                rs.getString("username"),
                rs.getString("full_name"),
                rs.getString("role"),
                rs.getString("status")
        );
    }
}
