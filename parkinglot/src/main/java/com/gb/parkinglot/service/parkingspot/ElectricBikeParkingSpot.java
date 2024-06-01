package com.gb.parkinglot.service.parkingspot;

import com.gb.parkinglot.model.parking.ParkingSpotType;

public class ElectricBikeParkingSpot extends ParkingSpot {
    public ElectricBikeParkingSpot(String id) {
        super(id, ParkingSpotType.EBIKE);
    }
}
