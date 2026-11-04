
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;

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
    }

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
            System.out.println("5. Logout");
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
                    approveAccount();
                    pause();
                }

                case "4" -> {
                    updateUser(currentAdmin);
                    pause();
                }

                case "5" -> {
                    System.out.println("Admin logout successful!");
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

        if (currentAdmin != null
                && currentAdmin.getUserId() == targetUserId
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
                targetUserId, newRole, newStatus)) {

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

    // INPUT VALIDATION: INTEGER

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

    // INPUT VALIDATION: ROLE

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

    // INPUT VALIDATION: STATUS

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

    private void approveAccount() {

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("\nNo pending registrations found.");
            return;
        }

        System.out.println("\n--- PENDING REGISTRATIONS ---");
        displayUsers(pendingUsers);

        System.out.println("Enter 0 to cancel approval.");

        int userId = readIntInput("Enter User ID to approve: ");

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

        if (userDao.approveUser(userId)) {
            System.out.println("Account approved successfully!");
            System.out.println("New status: APPROVED");
        } else {
            System.out.println(
                    "Approval failed. The account may no longer be pending."
            );
        }
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

    private void pause() {
        System.out.println("\nPress Enter to continue...");
        scanner.nextLine();
    }
}
