package com.scc.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    private static final String CREATE_USERS_SQL = """
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
                    status IN (
                        'PENDING',
                        'APPROVED',
                        'Active',
                        'Inactive'
                    )
                )
        )
        """;

    private static final String CREATE_LOGS_SQL = """
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

    public static boolean initialize() {

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            String usersTableSQL = getUsersTableSQL(statement);

            if (usersTableSQL == null) {
                statement.executeUpdate(CREATE_USERS_SQL);
            } else if (needsMigration(usersTableSQL)) {
                migrateUsersTable(connection);
            }

            try (Statement logStatement = connection.createStatement()) {
                logStatement.executeUpdate(CREATE_LOGS_SQL);
            }

            System.out.println("Database initialized successfully.");
            return true;

        } catch (SQLException e) {
            System.out.println(
                    "Database initialization failed: " + e.getMessage()
            );
            return false;
        }
    }

    private static String getUsersTableSQL(Statement statement)
            throws SQLException {

        String sql = """
            SELECT sql
            FROM sqlite_master
            WHERE type = 'table' AND name = 'USERS'
            """;

        try (ResultSet rs = statement.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString("sql");
            }
        }

        return null;
    }

    private static boolean needsMigration(String tableSQL) {
        String normalized = tableSQL.toUpperCase();

        return !normalized.contains("'PENDING'")
                || !normalized.contains("'APPROVED'")
                || !normalized.contains("'INACTIVE'");
    }

    private static void migrateUsersTable(Connection connection)
            throws SQLException {

        boolean originalAutoCommit = connection.getAutoCommit();

        try (Statement statement = connection.createStatement()) {

            // Disable foreign keys before starting the migration transaction.
            statement.execute("PRAGMA foreign_keys = OFF");

            connection.setAutoCommit(false);

            try {
                statement.executeUpdate("""
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
                                status IN (
                                    'PENDING',
                                    'APPROVED',
                                    'Active',
                                    'Inactive'
                                )
                            )
                    )
                    """);

                statement.executeUpdate("""
                    INSERT INTO USERS_NEW
                        (user_id, username, password,
                         full_name, role, status)
                    SELECT
                        user_id,
                        username,
                        password,
                        full_name,
                        role,
                        CASE
                            WHEN status IN (
                                'PENDING', 'APPROVED', 'Active', 'Inactive'
                            )
                            THEN status
                            ELSE 'PENDING'
                        END
                    FROM USERS
                    """);

                statement.executeUpdate("DROP TABLE USERS");
                statement.executeUpdate(
                        "ALTER TABLE USERS_NEW RENAME TO USERS"
                );

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
                statement.execute("PRAGMA foreign_keys = ON");
            }
        }
    }
}