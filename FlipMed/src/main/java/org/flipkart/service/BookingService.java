package org.flipkart.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.Booking;
import org.flipkart.entity.Slots;
import org.flipkart.exception.BookingNotFoundException;
import org.flipkart.exception.SlotUnavailableException;
import org.flipkart.helper.ServiceFactory;
import org.flipkart.util.UniqueIdGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BookingService {
    private UserService userService = ServiceFactory.getUserService();
    private Map<String, Booking> patientBookingMap = new HashMap<>();
    private Map<String, Booking> bookingIdMap = new HashMap<>();
    private static final Logger log = LogManager.getLogger(BookingService.class);

    public String bookSlot(String patientName, String doctorName, String slot) throws SlotUnavailableException {
        if (isAvailable(doctorName, slot) && !patientBookingMap.containsKey(patientName + slot)) {
            Slots slots = userService.markDoctorUnavailable(doctorName, slot);
            String bookingId = "B" + UniqueIdGenerator.generateNewID();
            Booking booking = new Booking(bookingId, patientName, doctorName, slots.getStartTime(), slots.getEndTime());
            patientBookingMap.put(patientName + slot, booking);
            bookingIdMap.put(bookingId, booking);
            return bookingId;
        }
        throw new SlotUnavailableException("Slot is not available");
    }

    private boolean isAvailable(String doctorName, String slot) {
        return userService.isDoctorAvailable(doctorName, slot);
    }

    public void cancelSlot(String bookingId) throws BookingNotFoundException {
        if (bookingIdMap.containsKey(bookingId)) {
            Booking booking = bookingIdMap.get(bookingId);
            List<String> slotsList = List.of(booking.getStartTime() + "-" + booking.getEndTime());
            userService.markDoctorAvail(booking.getDoctorName(), slotsList);
            bookingIdMap.remove(bookingId);
            log.info("Booking cancelled successfully");
        } else {
            throw new BookingNotFoundException("Booking not found");
        }

    }

    public List<Booking> showBookedAppointments() {
        return new ArrayList<>(bookingIdMap.values());
    }
}
