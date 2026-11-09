
package com.scc.ui;

import com.scc.dao.ActivityLogDao;
import com.scc.model.User;

import java.sql.SQLException;
import java.util.Scanner;

public class Authentication {

    private final MainMenu menu;
    private final AdminMenu adminMenu;
    private final ClearanceMenu clearanceMenu;
    private final ActivityLogDao activityLogDao;

    private boolean running = true;

    public Authentication(
            MainMenu menu,
            AdminMenu adminMenu,
            Scanner scanner) {

        this.menu = menu;
        this.adminMenu = adminMenu;
        this.clearanceMenu = new ClearanceMenu(scanner);
        this.activityLogDao = new ActivityLogDao();
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

        // Record successful login
        recordAuthenticationLog(
                user.getUserId(),
                "USER_LOGIN",
                user.getUsername() + " successfully logged in."
        );

        switch (user.getRole()) {

            case "BARANGAY_ADMINISTRATOR" -> {
                System.out.println(
                        "\nOpening Administrator Management..."
                );

                adminMenu.start(user);
            }

            case "BARANGAY_STAFF" -> {
                System.out.println(
                        "\nOpening Barangay Clearance Management..."
                );

                clearanceMenu.start(user);

                menu.logout();

                recordAuthenticationLog(
                        user.getUserId(),
                        "USER_LOGOUT",
                        user.getUsername() + " logged out."
                );
            }

            case "BARANGAY_OFFICIAL" -> {
                System.out.println("\n--- BARANGAY OFFICIAL ---");
                System.out.println("Welcome to your account.");

                menu.waitForEnter();
                menu.logout();

                recordAuthenticationLog(
                        user.getUserId(),
                        "USER_LOGOUT",
                        user.getUsername() + " logged out."
                );
            }

            default -> System.out.println("Unknown user role.");
        }
    }

    private void recordAuthenticationLog(
            int userId,
            String action,
            String description) {

        try {
            activityLogDao.logAction(
                    userId,
                    action,
                    description
            );

        } catch (SQLException e) {
            System.out.println(
                    "Warning: Audit log could not be saved: "
                            + e.getMessage()
            );
        }
    }
}
