package com.gb.parkinglot.service.parkingspot;

import com.gb.parkinglot.model.parking.ParkingSpotType;

public class LargeVehicleParkingSpot extends ParkingSpot {
    public LargeVehicleParkingSpot(String id) {
        super(id, ParkingSpotType.LARGE);
    }
}
