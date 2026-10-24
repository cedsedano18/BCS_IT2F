package com.scc.ui;

import com.scc.model.User;

public class Authentication {

    private final MainMenu menu;
    private final AdminMenu adminMenu;
    private boolean running = true;

    public Authentication(MainMenu menu, AdminMenu adminMenu) {
        this.menu = menu;
        this.adminMenu = adminMenu;
    }

    public void start() {

        System.out.println(
                "Starting Barangay Clearance Issuance System..."
        );

        while (running) {

            int choice = menu.showMenu();

            switch (choice) {
                case 1 -> {
                    menu.registerAccount();
                    menu.waitForEnter();
                }

                case 2 -> handleLogin();

                case 3 -> {
                    System.out.println(
                            "Thank you for using the system!"
                    );
                    running = false;
                }

                default -> {
                    System.out.println(
                            "Invalid choice. Please try again."
                    );
                    menu.waitForEnter();
                }
            }
        }
    }

    private void handleLogin() {

        User user = menu.loginAccount();

        if (user == null) {
            menu.waitForEnter();
            return;
        }

        switch (user.getRole()) {

            case "BARANGAY_ADMINISTRATOR" -> {
                System.out.println(
                        "\nOpening Administrator Management..."
                );
                adminMenu.start();
            }

            case "BARANGAY_STAFF" -> {
                System.out.println("\n--- BARANGAY STAFF ---");
                System.out.println("Welcome to your account.");
                menu.waitForEnter();
                menu.logout();
            }

            case "BARANGAY_OFFICIAL" -> {
                System.out.println("\n--- BARANGAY OFFICIAL ---");
                System.out.println("Welcome to your account.");
                menu.waitForEnter();
                menu.logout();
            }

            default -> System.out.println("Unknown user role.");
        }
    }
}