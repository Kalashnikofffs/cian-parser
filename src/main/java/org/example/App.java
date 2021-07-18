package org.example;

import java.io.IOException;
import java.util.List;

public class App {
    public static void main(String[] args) throws InterruptedException, IOException {

        SeleniumParser parser = new SeleniumParser();
        List<Apartment> apartments = parser.getApartments(args[0]);
        ExcelWriter.writeToFile(apartments);

    }
}
