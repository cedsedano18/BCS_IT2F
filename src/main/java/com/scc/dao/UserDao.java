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
                (username, password_hash, full_name, role)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            // Save the actual password instead of its hash.
            ps.setString(2, password);

            ps.setString(3, fullName);
            ps.setString(4, role);

            ps.executeUpdate();

            System.out.println("Registration successful!");
            System.out.println("Account status: PENDING");

            return true;

        } catch (SQLException e) {
            System.out.println(
                    "Registration failed: " + e.getMessage()
            );
            return false;
        }
    }

    public User login(String username, String password) {

        String sql = """
                SELECT * FROM USERS
                WHERE username = ?
                AND password_hash = ?
                AND status = 'Active'
                """;

        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, username);

            // Compare the entered password directly.
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    return new User(
                            rs.getInt("user_id"),
                            rs.getString("username"),
                            rs.getString("full_name"),
                            rs.getString("role"),
                            rs.getString("status")
                    );
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Login error: " + e.getMessage()
            );
        }

        return null;
    }
}