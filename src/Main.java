import java.util.Scanner;
import java.lang.System;
public class Main {
    public static void main(String[] args) {
        DatabaseProcessor dbProcessor = new DatabaseProcessor();
        Scanner scanner = new Scanner(System.in);
        int authOption;
        String isAuthenticated;
        int databaseOption;
        do {

            System.out.println("======================================================");
            System.out.println("$$$$$$$$$$$ Welcome to Database $$$$$$$$$$$$$$");
            System.out.println("======================================================");
            System.out.println();
            System.out.println("1. Login");
            System.out.println("2. SignUp");
            System.out.println("3. Exit");
            System.out.print("Select one of the option above : ");
            authOption = scanner.nextInt();
            if(authOption > 2 ) break;
            Authentication user = new Authentication();
            isAuthenticated = user.authenticate(authOption); //"Ashish";

            do {
                if(isAuthenticated.equals("null"))
                    break;

                System.out.println("$$$$$$$$$$$ Enter your input $$$$$$$$$$$$$$");
                System.out.println();
                System.out.println("1. Create Database");
                System.out.println("2. Select Database");
                System.out.println("3. Show Tables");
                System.out.println("4. Perform Query");
                System.out.println("5. Exit");
                databaseOption = scanner.nextInt();
                dbProcessor.performOperation(databaseOption,isAuthenticated);

            }while(databaseOption > 0 && databaseOption < 5);
        }while(authOption == 1 || authOption == 2);
        scanner.close();
    }
}
