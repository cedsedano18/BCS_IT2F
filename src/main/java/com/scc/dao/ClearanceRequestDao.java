
package com.scc.dao;

import com.scc.model.ClearanceRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ClearanceRequestDao {

    private final ActivityLogDao activityLogDao = new ActivityLogDao();

    // Create a new clearance request
    public boolean createRequest(
            String residentName,
            String address,

            String purpose,
            int encodedBy) {

        String sql = """
            INSERT INTO CLEARANCE_REQUESTS
            (resident_name, address, purpose, status, encoded_by)
            VALUES (?, ?, ?, 'PENDING', ?)
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, residentName);
            statement.setString(2, address);

            statement.setString(3, purpose);
            statement.setInt(4, encodedBy);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try {
                    activityLogDao.logAction(
                            encodedBy,
                            "CLEARANCE_REQUEST_CREATED",
                            "Created clearance request for resident: " + residentName
                    );
                } catch (SQLException e) {
                    System.out.println(
                            "Warning: Request saved, but audit log failed: "
                                    + e.getMessage()
                    );
                }

                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error creating clearance request: " + e.getMessage());
        }

        return false;
    }

    // Retrieve all clearance requests
    public List<ClearanceRequest> getAllRequests() {

        List<ClearanceRequest> requests = new ArrayList<>();

        String sql = """
            SELECT request_id, resident_name, address, purpose,
                   status, encoded_by, created_at, updated_at
            FROM CLEARANCE_REQUESTS
            ORDER BY request_id DESC
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                requests.add(new ClearanceRequest(
                        resultSet.getInt("request_id"),
                        resultSet.getString("resident_name"),
                        resultSet.getString("address"),
                        resultSet.getString("purpose"),
                        resultSet.getString("status"),
                        resultSet.getInt("encoded_by"),
                        resultSet.getString("created_at"),
                        resultSet.getString("updated_at")
                ));
            }

        } catch (SQLException e) {
            System.out.println("Error retrieving clearance requests: " + e.getMessage());
        }

        return requests;
    }

    // Update clearance request status
    public boolean updateRequestStatus(
            int requestId,
            String newStatus,
            int updatedBy) {

        String sql = """
            UPDATE CLEARANCE_REQUESTS
            SET status = ?,
                updated_at = datetime('now', 'localtime')
            WHERE request_id = ?
            """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, newStatus.toUpperCase());
            statement.setInt(2, requestId);

            int rows = statement.executeUpdate();

            if (rows > 0) {
                try {
                    activityLogDao.logAction(
                            updatedBy,
                            "CLEARANCE_STATUS_UPDATED",
                            "Updated clearance request ID " + requestId
                                    + " to status " + newStatus.toUpperCase()
                    );
                } catch (SQLException e) {
                    System.out.println(
                            "Warning: Status updated, but audit log failed: "
                                    + e.getMessage()
                    );
                }

                return true;
            }

        } catch (SQLException e) {
            System.out.println("Error updating clearance status: " + e.getMessage());
        }

        return false;
    }
}
