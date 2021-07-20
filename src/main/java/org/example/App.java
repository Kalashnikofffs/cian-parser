package org.example;

import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws InterruptedException, IOException {

        System.out.println("Please enter link: ");
        Scanner scanner = new Scanner(System.in);
        String link = scanner.nextLine();
        System.out.println("Please enter username: ");
        String username = scanner.nextLine();
        System.out.println("Please enter password:");
        String password = scanner.nextLine();

        SeleniumParser parser = new SeleniumParser(username, password);
        List<Apartment> apartments = parser.getApartments(link);
        ExcelWriter.writeToFile(apartments);

    }
}
