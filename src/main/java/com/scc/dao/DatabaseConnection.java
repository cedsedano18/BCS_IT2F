
package com.scc.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:barangay.db";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    public static void initializeDatabase() {
        String[] sql = {
                """
            CREATE TABLE IF NOT EXISTS USERS (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL CHECK (
                    role IN (
                        'BARANGAY_ADMINISTRATOR',
                        'BARANGAY_STAFF',
                        'BARANGAY_OFFICIAL'
                    )
                ),
                status TEXT NOT NULL DEFAULT 'PENDING'
                CHECK (status IN ('PENDING', 'Active', 'Inactive'))
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS ACTIVITY_LOGS (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT NOT NULL,
                description TEXT,
                log_timestamp TEXT NOT NULL DEFAULT
                    (datetime('now', 'localtime')),
                FOREIGN KEY (user_id) REFERENCES USERS(user_id)
            )
            """
        };

        try (Connection con = getConnection();
             Statement st = con.createStatement()) {

            st.execute("PRAGMA foreign_keys = ON");

            for (String query : sql) {
                st.executeUpdate(query);
            }

            System.out.println("Database ready!");

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }
}
