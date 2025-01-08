package com.example.fitness;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AdminDashboard {

	public static void showDashboardOptions(String username) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        while (true) {
            // Display options
            System.out.println("------ Admin Dashboard ------");
            System.out.println("1. Users Management");
            System.out.println("2. Content Management");
            System.out.println("3. System Logs");
            System.out.println("4. Logout/Exit");
            System.out.print("Please select an option: ");

            String line = scanner.nextLine();
            try {
                choice = Integer.parseInt(line);

                switch (choice) {
                    case 1:
                        
                        UserManagement.manageUsers();
                        break;
                    case 2:
                        ContentManagement.manageContent(username);
                        break;
                    case 3:
                        SystemLogs.manageLogs();
                        break;
                    case 4:
                        System.out.println("Logging out...");
                        return;  // Exit the loop and return
                    default:
                        System.out.println("Invalid option. Please select a valid option.");
                        break;
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input! Please enter a number.");
            }
        }
    }
}
