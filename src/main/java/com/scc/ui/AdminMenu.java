
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.dao.ActivityLogDao;
import com.scc.model.User;
import com.scc.model.ActivityLog;

import java.util.List;
import java.util.Scanner;
import java.sql.SQLException;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;
    private final ActivityLogDao activityLogDao;

    private static final String[] VALID_ROLES = {
            "BARANGAY_ADMINISTRATOR",
            "BARANGAY_STAFF",
            "BARANGAY_OFFICIAL"
    };

    private static final String[] VALID_STATUSES = {
            "APPROVED",
            "PENDING"
    };

    public AdminMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
        this.activityLogDao = new ActivityLogDao();
    }

    // ADMIN MAIN MENU

    public void start(User currentAdmin) {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("       ADMINISTRATOR MANAGEMENT");
            System.out.println("========================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Update User Role and Status");
            System.out.println("5. View System Audit Logs");
            System.out.println("6. Logout");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> {
                    displayUsers(userDao.getAllUsers());
                    pause();
                }

                case "2" -> {
                    displayUsers(userDao.getPendingUsers());
                    pause();
                }

                case "3" -> {
                    approveAccount(currentAdmin);
                    pause();
                }

                case "4" -> {
                    updateUser(currentAdmin);
                    pause();
                }

                case "5" -> viewAuditLogs(currentAdmin);

                case "6" -> {

                    // RECORD ADMIN LOGOUT

                    try {
                        activityLogDao.logAction(
                                currentAdmin.getUserId(),
                                "USER_LOGOUT",
                                "Admin " + currentAdmin.getUsername()
                                        + " (ID: " + currentAdmin.getUserId()
                                        + ") logged out."
                        );

                        System.out.println(
                                "Admin logout successful!"
                        );

                    } catch (SQLException e) {

                        System.out.println(
                                "Warning: Logout successful, but audit log "
                                        + "could not be saved: "
                                        + e.getMessage()
                        );
                    }

                    running = false;
                }

                default -> System.out.println(
                        "Invalid choice. Please try again."
                );
            }
        }
    }

    // UPDATE USER FEATURE

    private void updateUser(User currentAdmin) {

        if (currentAdmin == null) {
            System.out.println("Admin session not found.");
            return;
        }

        List<User> users = userDao.getAllUsers();

        if (users.isEmpty()) {
            System.out.println("\nNo user records found.");
            return;
        }

        System.out.println("\n========== UPDATE USER ==========");
        displayUsers(users);

        System.out.println("Enter 0 at any step to cancel.");

        int targetUserId = readIntInput(
                "\nEnter User ID to update: "
        );

        if (targetUserId == 0) {
            System.out.println("Update cancelled.");
            return;
        }

        User targetUser = users.stream()
                .filter(user -> user.getUserId() == targetUserId)
                .findFirst()
                .orElse(null);

        if (targetUser == null) {
            System.out.println("User ID not found.");
            return;
        }

        // SELF-EDIT SECURITY RESTRICTION

        if (currentAdmin.getUserId() == targetUserId
                && currentAdmin.getRole().equalsIgnoreCase(
                "BARANGAY_ADMINISTRATOR")
                && (currentAdmin.getStatus().equalsIgnoreCase("APPROVED")
                || currentAdmin.getStatus().equalsIgnoreCase("Active"))) {

            System.out.println(
                    "WARNING: Modifying the currently active admin account is restricted."
            );
            return;
        }

        System.out.println("\nSelected User:");
        System.out.println("User ID: " + targetUser.getUserId());
        System.out.println("Username: " + targetUser.getUsername());
        System.out.println("Current Role: " + targetUser.getRole());
        System.out.println("Current Status: " + targetUser.getStatus());

        String newRole = readValidRole();

        if (newRole == null) {
            System.out.println("Update cancelled.");
            return;
        }

        String newStatus = readValidStatus();

        if (newStatus == null) {
            System.out.println("Update cancelled.");
            return;
        }

        System.out.println("\nConfirm User Update");
        System.out.println("New Role: " + newRole);
        System.out.println("New Status: " + newStatus);

        if (userDao.updateUserRoleAndStatus(
                targetUserId,
                newRole,
                newStatus,
                currentAdmin.getUserId())) {

            System.out.println("\n==================================");
            System.out.println("USER UPDATED SUCCESSFULLY!");
            System.out.println("User ID: " + targetUserId);
            System.out.println("New Role: " + newRole);
            System.out.println("New Status: " + newStatus);
            System.out.println("==================================");

        } else {
            System.out.println(
                    "Update failed. Please check the database."
            );
        }
    }

    // INTEGER INPUT VALIDATION

    private int readIntInput(String prompt) {

        while (true) {

            System.out.print(prompt);

            String input = scanner.nextLine().trim();

            try {
                return Integer.parseInt(input);

            } catch (NumberFormatException e) {
                System.out.println(
                        "Invalid input! Please enter a numeric ID."
                );
            }
        }
    }

    // ROLE VALIDATION

    private String readValidRole() {

        while (true) {

            System.out.println("\nAvailable Roles:");
            System.out.println("1. BARANGAY_ADMINISTRATOR");
            System.out.println("2. BARANGAY_STAFF");
            System.out.println("3. BARANGAY_OFFICIAL");
            System.out.println("0. Cancel");

            System.out.print("Enter new role: ");

            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            String role = switch (input) {
                case "1" -> VALID_ROLES[0];
                case "2" -> VALID_ROLES[1];
                case "3" -> VALID_ROLES[2];
                default -> input.toUpperCase();
            };

            for (String validRole : VALID_ROLES) {
                if (role.equals(validRole)) {
                    return role;
                }
            }

            System.out.println(
                    "Invalid role! Please select a valid role."
            );
        }
    }

    // STATUS VALIDATION

    private String readValidStatus() {

        while (true) {

            System.out.println("\nAvailable Statuses:");
            System.out.println("1. APPROVED");
            System.out.println("2. PENDING");
            System.out.println("0. Cancel");

            System.out.print("Enter new status: ");

            String input = scanner.nextLine().trim();

            if (input.equals("0")) {
                return null;
            }

            String status = switch (input) {
                case "1" -> VALID_STATUSES[0];
                case "2" -> VALID_STATUSES[1];
                default -> input.toUpperCase();
            };

            for (String validStatus : VALID_STATUSES) {
                if (status.equals(validStatus)) {
                    return status;
                }
            }

            System.out.println(
                    "Invalid status! Please enter APPROVED or PENDING."
            );
        }
    }

    // APPROVE ACCOUNT

    private void approveAccount(User currentAdmin) {

        if (currentAdmin == null) {
            System.out.println("Admin session not found.");
            return;
        }

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("\nNo pending registrations found.");
            return;
        }

        System.out.println("\n--- PENDING REGISTRATIONS ---");
        displayUsers(pendingUsers);

        System.out.println("Enter 0 to cancel approval.");

        int userId = readIntInput(
                "Enter User ID to approve: "
        );

        if (userId == 0) {
            System.out.println("Approval cancelled.");
            return;
        }

        boolean exists = pendingUsers.stream()
                .anyMatch(user -> user.getUserId() == userId);

        if (!exists) {
            System.out.println(
                    "User ID not found in pending registrations."
            );
            return;
        }

        if (userDao.approveUser(
                userId,
                currentAdmin.getUserId())) {

            System.out.println("Account approved successfully!");
            System.out.println("New status: APPROVED");

        } else {
            System.out.println(
                    "Approval failed. The account may no longer be pending."
            );
        }
    }

    // SYSTEM AUDIT LOG MENU

    private void viewAuditLogs(User currentAdmin) {

        boolean viewing = true;

        while (viewing) {

            System.out.println("\n========================================");
            System.out.println("          SYSTEM AUDIT LOGS");
            System.out.println("========================================");
            System.out.println("1. View All System Logs");
            System.out.println("2. Filter Logs by User ID");
            System.out.println("3. Clear Historical Logs");
            System.out.println("4. Back to Admin Menu");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {

                case "1" -> {

                    try {
                        List<ActivityLog> logs =
                                activityLogDao.getAllLogs();

                        displayAuditLogs(logs);

                    } catch (SQLException e) {
                        System.out.println(
                                "Error retrieving logs: " + e.getMessage()
                        );
                    }

                    pause();
                }

                case "2" -> {

                    int userId = readIntInput(
                            "Enter User ID to filter: "
                    );

                    if (userId == 0) {
                        System.out.println("Filtering cancelled.");
                        break;
                    }

                    try {
                        List<ActivityLog> logs =
                                activityLogDao.getLogsByUserId(userId);

                        displayAuditLogs(logs);

                    } catch (SQLException e) {
                        System.out.println(
                                "Error filtering logs: " + e.getMessage()
                        );
                    }

                    pause();
                }

                case "3" -> {
                    clearHistoricalLogs(currentAdmin);
                    pause();
                }

                case "4" -> viewing = false;

                default -> System.out.println(
                        "Invalid choice. Please try again."
                );
            }
        }
    }

    // CLEAR HISTORICAL AUDIT LOGS

    private void clearHistoricalLogs(User currentAdmin) {

        if (currentAdmin == null) {
            System.out.println("Admin session not found.");
            return;
        }

        try {

            List<ActivityLog> logs =
                    activityLogDao.getAllLogs();

            if (logs == null || logs.isEmpty()) {
                System.out.println("\nNo historical logs available to clear.");
                return;
            }

            System.out.println("\n========================================");
            System.out.println("         CLEAR HISTORICAL LOGS");
            System.out.println("========================================");
            System.out.println("Total logs found: " + logs.size());

            System.out.println(
                    "\nWARNING: This action will permanently delete historical audit logs."
            );

            System.out.print("Are you sure you want to continue? (Y/N): ");

            String confirmation = scanner.nextLine().trim();

            if (!confirmation.equalsIgnoreCase("Y")) {
                System.out.println("Log clearing cancelled. No logs were deleted.");
                return;
            }

            int deletedCount = activityLogDao.clearAllLogs();

            System.out.println("\n========================================");
            System.out.println("HISTORICAL LOGS CLEARED SUCCESSFULLY!");
            System.out.println("Deleted Logs: " + deletedCount);
            System.out.println("Admin ID: " + currentAdmin.getUserId());
            System.out.println("========================================");

            // RECORD LOG CLEARING ACTION

            try {

                activityLogDao.logAction(
                        currentAdmin.getUserId(),
                        "LOGS_CLEARED",
                        "Administrator cleared " + deletedCount
                                + " historical activity logs."
                );

                System.out.println(
                        "Audit record for the clearing action was saved."
                );

            } catch (SQLException e) {

                System.out.println(
                        "Warning: Logs were cleared, but the clearing action "
                                + "could not be recorded: " + e.getMessage()
                );
            }

        } catch (SQLException e) {

            System.out.println(
                    "Error clearing historical logs: " + e.getMessage()
            );
        }
    }

    // DISPLAY AUDIT LOGS

    private void displayAuditLogs(List<ActivityLog> logs) {

        System.out.println(
                "\n================ SYSTEM AUDIT HISTORY ================"
        );

        if (logs == null || logs.isEmpty()) {
            System.out.println("No activity logs found.");
            return;
        }

        String border = "-".repeat(105);

        System.out.println(border);

        System.out.printf(
                "%-5s | %-8s | %-18s | %-35s | %-20s%n",
                "ID",
                "USER ID",
                "ACTION",
                "DESCRIPTION",
                "TIMESTAMP"
        );

        System.out.println(border);

        for (ActivityLog log : logs) {

            System.out.printf(
                    "%-5d | %-8d | %-18.18s | %-35.35s | %-20.20s%n",
                    log.getLogId(),
                    log.getUserId(),
                    log.getAction(),
                    log.getDescription(),
                    log.getLogTimestamp()
            );
        }

        System.out.println(border);
    }

    // DISPLAY USERS

    private void displayUsers(List<User> users) {

        System.out.println();

        if (users == null || users.isEmpty()) {
            System.out.println("No user records found.");
            return;
        }

        String border =
                "+-------+-----------------+--------------------------------+--------------+";

        System.out.println(border);

        System.out.printf(
                "| %-5s | %-15s | %-30s | %-12s |%n",
                "ID", "USERNAME", "ROLE", "STATUS"
        );

        System.out.println(border);

        for (User user : users) {

            System.out.printf(
                    "| %-5d | %-15.15s | %-30.30s | %-12.12s |%n",
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole(),
                    user.getStatus()
            );
        }

        System.out.println(border);
    }

    // PAUSE

    private void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
