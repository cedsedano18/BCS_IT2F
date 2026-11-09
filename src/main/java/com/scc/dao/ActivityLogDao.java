
package com.scc.dao;

import com.scc.model.ActivityLog;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {

    // INSERT ACTIVITY LOG

    public void logAction(
            int userId,
            String actionType,
            String description) throws SQLException {

        String sql = """
                INSERT INTO ACTIVITY_LOGS
                    (user_id, action, description)
                VALUES (?, ?, ?)
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setString(2, actionType);
            statement.setString(3, description);

            statement.executeUpdate();
        }
    }

    // GET ALL LOGS

    public List<ActivityLog> getAllLogs() throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT log_id, user_id, action,
                       description, log_timestamp
                FROM ACTIVITY_LOGS
                ORDER BY log_timestamp DESC, log_id DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                logs.add(new ActivityLog(
                        resultSet.getInt("log_id"),
                        resultSet.getInt("user_id"),
                        resultSet.getString("action"),
                        resultSet.getString("description"),
                        resultSet.getString("log_timestamp")
                ));
            }
        }

        return logs;
    }

    // GET LOGS BY USER ID

    public List<ActivityLog> getLogsByUserId(
            int userId) throws SQLException {

        List<ActivityLog> logs = new ArrayList<>();

        String sql = """
                SELECT log_id, user_id, action,
                       description, log_timestamp
                FROM ACTIVITY_LOGS
                WHERE user_id = ?
                ORDER BY log_timestamp DESC, log_id DESC
                """;

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                while (resultSet.next()) {

                    logs.add(new ActivityLog(
                            resultSet.getInt("log_id"),
                            resultSet.getInt("user_id"),
                            resultSet.getString("action"),
                            resultSet.getString("description"),
                            resultSet.getString("log_timestamp")
                    ));
                }
            }
        }

        return logs;
    }

    // CLEAR ALL HISTORICAL LOGS

    public int clearAllLogs() throws SQLException {

        String sql = "DELETE FROM ACTIVITY_LOGS";

        try (Connection connection =
                     DatabaseConnection.getConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            return statement.executeUpdate();
        }
    }
}
