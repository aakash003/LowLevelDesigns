package com.gb.parkinglot.service.parkingspot;

import com.gb.parkinglot.model.parking.ParkingSpotType;

public class MotorBikeParkingSpot extends ParkingSpot {
    public MotorBikeParkingSpot(String id) {
        super(id, ParkingSpotType.MOTORBIKE);
    }
}
