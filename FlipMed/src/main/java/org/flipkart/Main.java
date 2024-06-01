package org.flipkart;


import org.flipkart.entity.Booking;
import org.flipkart.entity.Doctor;
import org.flipkart.enums.Rank;
import org.flipkart.exception.BookingNotFoundException;
import org.flipkart.exception.SlotUnavailableException;
import org.flipkart.helper.ServiceFactory;
import org.flipkart.service.BookingService;
import org.flipkart.service.strategy.IRankingStrategy;
import org.flipkart.service.UserService;

import java.util.List;
import java.util.Scanner;

public class Main {


    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        UserService userService = ServiceFactory.getUserService();
        BookingService bookingService = ServiceFactory.getBookingService();

        IRankingStrategy iRankingStrategy = ServiceFactory.getRankingStrategy(Rank.HIGHEST_RATED);
        while (true) {
            String inp = scanner.nextLine();
            inp = inp.trim();
            String[] inpArr = inp.split("~");
            try {
                switch (inpArr[0]) {
                    case "registerDoc": {
                        String name = inpArr[1];
                        String speciality = inpArr[2];
                        userService.registerDoc(name, speciality);
                        break;
                    }
                    case "markDocAvail": {
                        String name = inpArr[1];
                        List<String> slots = List.of(inpArr[2].trim().split(","));
                        userService.markDoctorAvail(name, slots);
                        break;
                    }
                    case "showAvailByspeciality": {
                        String speciality = inpArr[1];
                        List<Doctor> slots = iRankingStrategy.findBySpeciality(speciality);
                        for (Doctor doctor : slots) {
                            System.out.println("Name=" + doctor.getName() + " " + "Speciality=" + doctor.getSpeciality() + " " + "Rating=" + doctor.getRating() + " " + doctor.getSlots());
                        }
                        break;
                    }
                    case "registerPatient":
                        String name = inpArr[1];
                        userService.registerPatient(name);
                        break;
                    case "bookAppointment":
                        String patientName = inpArr[1];
                        String doctorName = inpArr[2];
                        String slot = inpArr[3];
                        String bookingId = bookingService.bookSlot(patientName, doctorName, slot);
                        System.out.println("Booking successful with id: " + bookingId);
                        break;
                    case "cancelBookingId":
                        String bookinId = inpArr[1];
                        bookingService.cancelSlot(bookinId);
                        System.out.println("Booking cancellation done for id: " + bookinId);
                        break;
                    case "showAppointmentsBooked":
                        List<Booking> bookingList = bookingService.showBookedAppointments();
                        if (bookingList == null)
                            System.out.println("No bookings done yet");

                        for (Booking booking : bookingList) {
                            System.out.println("BookingId=" + booking.getBookingId() + " " + "PatientName=" + booking.getPatientName() + " " + "DoctorName=" + booking.getDoctorName() + " " + "StartTime=" + booking.getStartTime() + " " + "EndTime=" + booking.getEndTime());
                        }

                        break;
                }
            } catch (RuntimeException | SlotUnavailableException runtimeException) {
                System.out.println(runtimeException);
            } catch (BookingNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

    }

//    registerDoc~Curious~Cardiologist
//    markDocAvail~Curious~9:00-10:00
//    registerDoc~Dreadful~Dermatologist
//    showAvailByspeciality~Cardiologist
//    markDocAvail~Dreadful~9:00-10:00,11:00-12:00,13:00-14:00
//    registerPatient~PatientA
//    bookAppointment~PatientA~Curious~12:00
    // bookAppointment~PatientA~Dreadful~11:00


}