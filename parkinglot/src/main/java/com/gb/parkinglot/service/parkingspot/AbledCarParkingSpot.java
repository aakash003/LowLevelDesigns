package com.gb.parkinglot.service.parkingspot;

import com.gb.parkinglot.model.parking.ParkingSpotType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AbledCarParkingSpot extends ParkingSpot {
    public AbledCarParkingSpot(String id) {
        super(id, ParkingSpotType.ABLED);
    }
}
