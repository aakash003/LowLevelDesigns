package com.gb.parkinglot.service.gates;

import com.gb.parkinglot.model.parking.ParkingTicket;
import com.gb.parkinglot.model.parking.TicketStatus;
import com.gb.parkinglot.model.vehicle.Vehicle;
import com.gb.parkinglot.service.ParkingLot;
import com.gb.parkinglot.service.parkingspot.ParkingSpot;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class EntrancePanel {
    private String id;

    public EntrancePanel(String id) {
        this.id = id;
    }

    public ParkingTicket getParkingTicket(Vehicle vehicle) {
        if (!ParkingLot.INSTANCE.canPark(vehicle.getType()))
            return null;
        ParkingSpot parkingSpot = ParkingLot.INSTANCE.getParkingSpot(vehicle.getType());
        if (parkingSpot == null)
            return null;
        return buildTicket(vehicle.getLicenseNumber(), parkingSpot.getParkingSpotId());
    }

    private ParkingTicket buildTicket(String vehicleLicenseNumber, String parkingSpotId) {
        ParkingTicket parkingTicket = new ParkingTicket();
        parkingTicket.setIssuedAt(LocalDateTime.now());
        parkingTicket.setAllocatedSpotId(parkingSpotId);
        parkingTicket.setLicensePlateNumber(vehicleLicenseNumber);
        parkingTicket.setTicketNumber(UUID.randomUUID().toString());
        parkingTicket.setTicketStatus(TicketStatus.ACTIVE);
        return parkingTicket;
    }
}
