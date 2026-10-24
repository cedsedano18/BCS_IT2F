package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.AdminMenu;
import com.scc.ui.Authentication;
import com.scc.ui.MainMenu;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        System.out.println("Initializing database...");

        boolean initialized = DatabaseInitializer.initialize();

        if (!initialized) {
            System.out.println(
                    "System startup failed. Please check the database."
            );
            return;
        }

        try (Scanner scanner = new Scanner(System.in)) {

            UserDao userDao = new UserDao();

            MainMenu mainMenu = new MainMenu(scanner, userDao);
            AdminMenu adminMenu = new AdminMenu(scanner, userDao);

            Authentication authentication =
                    new Authentication(mainMenu, adminMenu);

            authentication.start();
        }
    }
}