package com.gb.parkinglot.service.parkingspot;

import com.gb.parkinglot.model.parking.ParkingSpotType;

public class CarParkingSpot extends ParkingSpot {
    public CarParkingSpot(String id) {
        super(id, ParkingSpotType.CAR);
    }
}
