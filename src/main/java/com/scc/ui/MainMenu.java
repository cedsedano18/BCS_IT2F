
package com.scc.ui;

import com.scc.dao.UserDao;
import com.scc.model.User;
import java.util.Scanner;

public class MainMenu {

    private final Scanner scanner;
    private final UserDao userDao;

    public MainMenu(Scanner scanner, UserDao userDao) {
        this.scanner = scanner;
        this.userDao = userDao;
    }

    public int showMenu() {
        System.out.println("\n==================================");
        System.out.println(" BARANGAY CLEARANCE ISSUANCE SYSTEM");
        System.out.println("==================================");
        System.out.println("1. Register Account");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Enter your choice: ");

        try {
            return Integer.parseInt(scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public void registerAccount() {
        System.out.println("\n--- REGISTER ACCOUNT ---");

        System.out.print("Full name: ");
        String fullName = scanner.nextLine().trim();

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        System.out.println("\nSelect role:");
        System.out.println("1. Barangay Administrator");
        System.out.println("2. Barangay Staff");
        System.out.println("3. Barangay Official");
        System.out.print("Choose role: ");

        String choice = scanner.nextLine().trim();

        String role = switch (choice) {
            case "1" -> "BARANGAY_ADMINISTRATOR";
            case "2" -> "BARANGAY_STAFF";
            case "3" -> "BARANGAY_OFFICIAL";
            default -> null;
        };

        if (fullName.isBlank()
                || username.isBlank()
                || password.isBlank()
                || role == null) {
            System.out.println("Invalid input. Please try again.");
            return;
        }

        userDao.register(username, password, fullName, role);
    }

    public User loginAccount() {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println(
                    "Login failed. Check your credentials "
                            + "or account status."
            );
            return null;
        }

        System.out.println("Login successful!");
        System.out.println("Welcome, " + user.getFullName());
        System.out.println("Role: " + user.getRole());

        return user;
    }

    public void waitForEnter() {
        System.out.println("Press Enter to continue...");
        scanner.nextLine();
    }

    public void logout() {
        System.out.println("Logout successful!");
    }
}
