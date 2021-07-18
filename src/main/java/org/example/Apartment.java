package org.example;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Apartment {

    private int amountOfRooms;
    private double sqr;
    private String flour;
    private String address;
    private long price;
    private String link;
    private String phoneNumber;

}
