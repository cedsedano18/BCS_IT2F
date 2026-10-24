package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class AdminMenu {

    private final Scanner scanner;
    private final UserDao userDao;

    public AdminMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public void start() {

        boolean running = true;

        while (running) {

            System.out.println("\n========================================");
            System.out.println("       ADMINISTRATOR MANAGEMENT");
            System.out.println("========================================");
            System.out.println("1. View All System Users");
            System.out.println("2. View Pending Registrations");
            System.out.println("3. Approve User Account");
            System.out.println("4. Logout");
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
                    System.out.println("Admin logout successful!");
                    running = false;
                }
                default -> System.out.println(
                        "Invalid choice. Please try again."
                );
            }
        }
    }

    private void approveAccount() {

        List<User> pendingUsers = userDao.getPendingUsers();

        if (pendingUsers.isEmpty()) {
            System.out.println("\nNo pending registrations found.");
            return;
        }

        System.out.println("\n--- PENDING REGISTRATIONS ---");
        displayUsers(pendingUsers);

        System.out.println("Enter 0 to cancel approval.");
        System.out.print("Enter User ID to approve: ");

        String input = scanner.nextLine().trim();

        if (input.equals("0")) {
            System.out.println("Approval cancelled.");
            return;
        }

        int userId;

        try {
            userId = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.out.println("Invalid User ID.");
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