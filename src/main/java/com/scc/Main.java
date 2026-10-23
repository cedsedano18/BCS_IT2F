
package com.scc;

import com.scc.dao.DatabaseInitializer;
import com.scc.dao.UserDao;
import com.scc.ui.MainMenu;
import com.scc.ui.Authentication;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        // Step 1: Initialize the database
        boolean initialized = DatabaseInitializer.initialize();

        if (!initialized) {
            System.out.println(
                    "System startup failed. Please check the database."
            );
            return;
        }

        // Step 2: Create shared dependencies
        Scanner scanner = new Scanner(System.in);
        UserDao userDao = new UserDao();

        // Step 3: Create the main menu
        MainMenu menu = new MainMenu(scanner, userDao);

        // Step 4: Start authentication
        Authentication authentication = new Authentication(menu);
        authentication.start();
    }
}
