package com.gb.parkinglot.service.gates;

import com.gb.parkinglot.model.parking.HourlyCost;
import com.gb.parkinglot.model.parking.ParkingSpotType;
import com.gb.parkinglot.model.parking.ParkingTicket;
import com.gb.parkinglot.service.ParkingLot;
import com.gb.parkinglot.service.parkingspot.ParkingSpot;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ExitPanel {
    private String id;

    public ParkingTicket scanAndVacate(ParkingTicket parkingTicket) {
        ParkingSpot parkingSpot =
                ParkingLot.INSTANCE.vacateParkingSpot(parkingTicket.getAllocatedSpotId());
        parkingTicket.setCharges(calculateCost(parkingTicket, parkingSpot.getParkingSpotType()));
        return parkingTicket;
    }

    private double calculateCost(ParkingTicket parkingTicket, ParkingSpotType parkingSpotType) {
        Duration duration = Duration.between(parkingTicket.getIssuedAt(), LocalDateTime.now());
        long hours = duration.toHours();
        if (hours == 0)
            hours = 1;
        double amount = hours * new HourlyCost().getCost(parkingSpotType);
        return amount;
    }
}
