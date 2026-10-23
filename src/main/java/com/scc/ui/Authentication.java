
package com.scc.ui;

import com.scc.model.User;

public class Authentication {

    private final MainMenu menu;
    private boolean running = true;

    public Authentication(MainMenu menu) {
        this.menu = menu;
    }

    public void start() {
        System.out.println(
                "Starting Barangay Clearance Issuance System..."
        );

        while (running) {
            int choice = menu.showMenu();

            switch (choice) {
                case 1 -> menu.registerAccount();

                case 2 -> handleLogin();

                case 3 -> {
                    System.out.println(
                            "Thank you for using the system!"
                    );
                    running = false;
                }

                default -> System.out.println(
                        "Invalid choice. Please try again."
                );
            }
        }
    }

    private void handleLogin() {
        User user = menu.loginAccount();

        if (user == null) {
            return;
        }

        switch (user.getRole()) {
            case "BARANGAY_ADMINISTRATOR" ->
                    showRoleMessage("Barangay Administrator");

            case "BARANGAY_STAFF" ->
                    showRoleMessage("Barangay Staff");

            case "BARANGAY_OFFICIAL" ->
                    showRoleMessage("Barangay Official");

            default -> {
                System.out.println("Unknown user role.");
                return;
            }
        }

        System.out.println("\nPress Enter to logout.");
        menu.waitForEnter();
        menu.logout();
    }

    private void showRoleMessage(String role) {
        System.out.println("\n--- " + role + " ---");
        System.out.println("Welcome to your account.");
    }
}
