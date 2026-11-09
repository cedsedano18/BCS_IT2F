
package com.scc.ui;

import com.scc.dao.ClearanceRequestDao;
import com.scc.model.ClearanceRequest;
import com.scc.model.User;

import java.util.List;
import java.util.Scanner;

public class ClearanceMenu {

    private final Scanner scanner;
    private final ClearanceRequestDao clearanceRequestDao;

    public ClearanceMenu(Scanner scanner) {
        this.scanner = scanner;
        this.clearanceRequestDao = new ClearanceRequestDao();
    }

    public void start(User currentUser) {

        boolean running = true;

        while (running) {
            System.out.println("\n========================================");
            System.out.println("       BARANGAY CLEARANCE MANAGEMENT");
            System.out.println("========================================");
            System.out.println("Staff: " + currentUser.getFullName());
            System.out.println("----------------------------------------");
            System.out.println("1. Encode New Clearance Request");
            System.out.println("2. View All Clearance Requests");
            System.out.println("3. Update Clearance Request Status");
            System.out.println("4. Logout");
            System.out.println("----------------------------------------");
            System.out.print("Enter your choice: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> encodeRequest(currentUser);
                case "2" -> viewAllRequests();
                case "3" -> updateRequestStatus(currentUser);
                case "4" -> {
                    System.out.println("Returning to main menu...");
                    running = false;
                }
                default -> System.out.println("Invalid choice! Please select 1-4.");
            }
        }
    }

    // Encode a new clearance request
    private void encodeRequest(User currentUser) {

        System.out.println("\n========== NEW CLEARANCE REQUEST ==========");

        System.out.print("Enter Resident Name: ");
        String residentName = scanner.nextLine().trim();

        if (residentName.isEmpty()) {
            System.out.println("Resident name cannot be empty!");
            return;
        }

        System.out.print("Enter Resident Address: ");
        String address = scanner.nextLine().trim();

        if (address.isEmpty()) {
            System.out.println("Address cannot be empty!");
            return;
        }

        System.out.print("Enter Purpose of Clearance: ");
        String purpose = scanner.nextLine().trim();

        if (purpose.isEmpty()) {
            System.out.println("Purpose cannot be empty!");
            return;
        }

        boolean success = clearanceRequestDao.createRequest(
                residentName,
                address,
                purpose,
                currentUser.getUserId()
        );

        if (success) {
            System.out.println("\nCLEARANCE REQUEST SAVED SUCCESSFULLY!");
            System.out.println("Resident Name: " + residentName);
            System.out.println("Status: PENDING");
        } else {
            System.out.println("\nFailed to save clearance request.");
        }

        pause();
    }

    // Display all clearance requests
    private void viewAllRequests() {

        System.out.println("\n================ ALL CLEARANCE REQUESTS ================");

        List<ClearanceRequest> requests =
                clearanceRequestDao.getAllRequests();

        if (requests.isEmpty()) {
            System.out.println("No clearance requests found.");
            pause();
            return;
        }

        System.out.printf(
                "%-8s %-22s %-20s %-18s %-12s%n",
                "ID", "RESIDENT", "ADDRESS", "PURPOSE", "STATUS"
        );

        System.out.println("--------------------------------------------------------------------------");

        for (ClearanceRequest request : requests) {
            System.out.printf(
                    "%-8d %-22s %-20s %-18s %-12s%n",
                    request.getRequestId(),
                    shorten(request.getResidentName(), 21),
                    shorten(request.getAddress(), 19),
                    shorten(request.getPurpose(), 17),
                    request.getStatus()
            );
        }

        System.out.println("--------------------------------------------------------------------------");
        pause();
    }

    // Update request status
    private void updateRequestStatus(User currentUser) {

        System.out.println("\n========== UPDATE CLEARANCE STATUS ==========");

        System.out.print("Enter Request ID: ");
        String input = scanner.nextLine().trim();

        int requestId;

        try {
            requestId = Integer.parseInt(input);

            if (requestId <= 0) {
                System.out.println("Invalid ID! Please enter a positive number.");
                pause();
                return;
            }

        } catch (NumberFormatException e) {
            System.out.println("Invalid input! Please enter a numeric ID.");
            pause();
            return;
        }

        System.out.println("\nSelect New Status:");
        System.out.println("1. APPROVED");
        System.out.println("2. REJECTED");
        System.out.println("3. ISSUED");
        System.out.print("Enter your choice: ");

        String choice = scanner.nextLine().trim();

        String newStatus;

        switch (choice) {
            case "1" -> newStatus = "APPROVED";
            case "2" -> newStatus = "REJECTED";
            case "3" -> newStatus = "ISSUED";
            default -> {
                System.out.println("Invalid status! Please select 1-3.");
                pause();
                return;
            }
        }

        boolean success = clearanceRequestDao.updateRequestStatus(
                requestId,
                newStatus,
                currentUser.getUserId()
        );

        if (success) {
            System.out.println("\nCLEARANCE STATUS UPDATED SUCCESSFULLY!");
            System.out.println("Request ID: " + requestId);
            System.out.println("New Status: " + newStatus);
        } else {
            System.out.println("\nFailed to update status. Please check the Request ID.");
        }

        pause();
    }

    private String shorten(String value, int maxLength) {
        if (value == null) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return value.substring(0, maxLength - 3) + "...";
    }

    private void pause() {
        System.out.println("\nPress ENTER to continue...");
        scanner.nextLine();
    }
}
