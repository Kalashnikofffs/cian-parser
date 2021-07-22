package org.example;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws InterruptedException, IOException {

        System.out.println("Please enter link: ");
        Scanner scanner = new Scanner(System.in);
        String link = "https://spb.cian.ru/cat.php?deal_type=sale&engine_version=2&offer_type=flat&region=2&room1=1&room2=1&room3=1";
        System.out.println("Please enter username: ");
        String username = "xohoke9968@godpeed.com";
        System.out.println("Please enter password:");
        String password = "Qwerty123456";
        System.out.println("Please enter directory where we should save excel document:");
        String directory = "C:\\Users\\skalashn\\Screenshots";

        SeleniumParser parser = new SeleniumParser(username, password);
        List<Apartment> apartments = parser.getApartments(link);
        ExcelWriter.writeToFile(directory, apartments);

    }
}
