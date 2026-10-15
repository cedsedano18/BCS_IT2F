
package com.scc;

import com.scc.dao.DatabaseConnection;
import com.scc.ui.MainMenu;

public class Main {
    public static void main(String[] args) {
        DatabaseConnection.initializeDatabase();

        MainMenu menu = new MainMenu();
        boolean running = true;

        while (running) {
            int choice = menu.showMenu();

            switch (choice) {
                case 1 -> menu.registerAccount();
                case 2 -> menu.loginAccount();
                case 3 -> {
                    System.out.println("Thank you for using the system!");
                    running = false;
                }
                default -> System.out.println(
                        "Invalid choice. Please try again."
                );
            }
        }
    }
}
