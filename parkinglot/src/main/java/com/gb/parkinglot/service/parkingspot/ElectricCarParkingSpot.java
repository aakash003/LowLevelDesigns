package com.gb.parkinglot.service.parkingspot;

import com.gb.parkinglot.model.parking.ParkingSpotType;

public class ElectricCarParkingSpot extends ParkingSpot {
    public ElectricCarParkingSpot(String id) {
        super(id, ParkingSpotType.ELECTRIC);
    }
}
