
package com.scc.dao;

import com.scc.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDao {

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

            // Save the entered password directly
            // for the classroom demonstration.
            ps.setString(2, password);

            ps.setString(3, fullName);
            ps.setString(4, role);

            ps.executeUpdate();

            System.out.println("Registration successful!");
            System.out.println("Account status: PENDING");
            System.out.println(
                    "Please wait for an administrator to activate your account.");

            return true;

        } catch (SQLException e) {
            if (e.getMessage() != null
                    && e.getMessage().toLowerCase().contains("unique")) {
                System.out.println(
                        "Registration failed: Username already exists.");
            } else {
                System.out.println(
                        "Registration failed: " + e.getMessage());
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
                AND status = 'Active'
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    String storedPassword = rs.getString("password");

                    boolean passwordMatches;

                    if (storedPassword != null
                            && storedPassword.matches("[0-9a-fA-F]{64}")) {

                        // Support existing SHA-256 hashed accounts.
                        passwordMatches =
                                PasswordUtil.verifyPassword(
                                        password, storedPassword);
                    } else {
                        // Support existing plain-text accounts.
                        passwordMatches =
                                password.equals(storedPassword);
                    }

                    if (passwordMatches) {
                        return new User(
                                rs.getInt("user_id"),
                                rs.getString("username"),
                                rs.getString("full_name"),
                                rs.getString("role"),
                                rs.getString("status")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Login error: " + e.getMessage());
        }

        return null;
    }
}
