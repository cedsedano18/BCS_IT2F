
package com.scc.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseInitializer {

    public static boolean initialize() {

        String createUsersTable = """
            CREATE TABLE USERS_NEW (
                user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL UNIQUE,
                password TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role TEXT NOT NULL CHECK (
                    role IN (
                        'BARANGAY_ADMINISTRATOR',
                        'BARANGAY_STAFF',
                        'BARANGAY_OFFICIAL'
                    )
                ),
                status TEXT NOT NULL DEFAULT 'PENDING'
                    CHECK (
                        status IN ('PENDING', 'Active', 'Inactive')
                    )
            )
            """;

        String createLogsTable = """
            CREATE TABLE IF NOT EXISTS ACTIVITY_LOGS (
                log_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id INTEGER,
                action TEXT NOT NULL,
                description TEXT,
                log_timestamp TEXT NOT NULL
                    DEFAULT (datetime('now', 'localtime')),
                FOREIGN KEY (user_id)
                    REFERENCES USERS(user_id)
            )
            """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            // Check whether the existing USERS table needs migration.
            String tableSQL = null;

            try (ResultSet rs = statement.executeQuery(
                    "SELECT sql FROM sqlite_master " +
                            "WHERE type = 'table' AND name = 'USERS'")) {

                if (rs.next()) {
                    tableSQL = rs.getString("sql");
                }
            }

            boolean needsMigration =
                    tableSQL != null &&
                            (!tableSQL.contains("'PENDING'") ||
                                    !tableSQL.contains("'Inactive'"));

            if (tableSQL == null) {
                // No USERS table exists yet.
                statement.executeUpdate("""
                    CREATE TABLE USERS (
                        user_id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        password TEXT NOT NULL,
                        full_name TEXT NOT NULL,
                        role TEXT NOT NULL CHECK (
                            role IN (
                                'BARANGAY_ADMINISTRATOR',
                                'BARANGAY_STAFF',
                                'BARANGAY_OFFICIAL'
                            )
                        ),
                        status TEXT NOT NULL DEFAULT 'PENDING'
                            CHECK (
                                status IN ('PENDING', 'Active', 'Inactive')
                            )
                    )
                    """);

            } else if (needsMigration) {

                // Foreign-key enforcement must be disabled
                // before starting the transaction.
                statement.execute("PRAGMA foreign_keys = OFF");

                connection.setAutoCommit(false);

                try {
                    // Remove the leftover temporary table from
                    // the previous failed migration.
                    statement.executeUpdate(
                            "DROP TABLE IF EXISTS USERS_NEW");

                    statement.executeUpdate(createUsersTable);

                    // Copy existing accounts without changing
                    // their IDs or account information.
                    statement.executeUpdate("""
                        INSERT INTO USERS_NEW
                            (user_id, username, password,
                             full_name, role, status)
                        SELECT
                            user_id, username, password,
                            full_name, role,
                            CASE
                                WHEN status IN
                                    ('PENDING', 'Active', 'Inactive')
                                THEN status
                                ELSE 'PENDING'
                            END
                        FROM USERS
                        """);

                    statement.executeUpdate("DROP TABLE USERS");

                    statement.executeUpdate(
                            "ALTER TABLE USERS_NEW RENAME TO USERS");

                    connection.commit();

                } catch (SQLException e) {
                    connection.rollback();
                    throw e;

                } finally {
                    connection.setAutoCommit(true);
                    statement.execute("PRAGMA foreign_keys = ON");
                }
            }

            // Ensure the activity log table exists.
            statement.executeUpdate(createLogsTable);

            System.out.println(
                    "Database initialized successfully.");

            return true;

        } catch (SQLException e) {
            System.out.println(
                    "Database initialization failed: "
                            + e.getMessage());

            return false;
        }
    }
}
