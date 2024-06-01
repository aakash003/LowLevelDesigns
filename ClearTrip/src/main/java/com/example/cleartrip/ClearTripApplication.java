package com.example.cleartrip;

import com.example.cleartrip.model.Activities;
import static com.example.cleartrip.model.Activities.Cardio;
import static com.example.cleartrip.model.Activities.Weights;
import static com.example.cleartrip.model.Activities.Yoga;
import com.example.cleartrip.model.Timings;
import com.example.cleartrip.service.CenterService;
import com.example.cleartrip.service.AdminService;
import com.example.cleartrip.service.BookingService;
import com.example.cleartrip.storage.Storage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import java.util.List;

@SpringBootApplication
public class ClearTripApplication {


    public static void main(String[] args) {
        ApplicationContext cts = SpringApplication.run(ClearTripApplication.class, args);

        CenterService centerService = cts.getBean(CenterService.class);
        AdminService adminService = cts.getBean(AdminService.class);
        BookingService bookingService = cts.getBean(BookingService.class);
        Storage storage = cts.getBean(Storage.class);


        centerService.addCenter("Koramangala");

        List<Timings> timingsList = List.of(new Timings(6, 9), new Timings(18, 21));

        centerService.addCenterTimings("Koramangala", timingsList);

        List<Activities> activities = List.of(Weights, Cardio, Yoga);
        centerService.addCenterActivities("Koramangala", activities);

        adminService.addWorkout("Koramangala", Weights, 6, 7, 100);
        adminService.addWorkout("Koramangala", Cardio, 7, 8, 150);
        adminService.addWorkout("Koramangala", Yoga, 8, 9, 200);

        System.out.println(storage.getPlaceActivitiesMap());
        System.out.println(storage.getPlaceWorkoutMap());
        System.out.println(storage.getPlaceTimingsMap());


        List<String> availableSlots = bookingService.getWorkOutChart(Weights);
        System.out.println("Available Slots" + availableSlots);


        bookingService.bookSession("Vaibhava", "Koramangala", Weights, 6, 7);

        List<String> availablaSlots2 = bookingService.getWorkOutChart(Weights);
        System.out.println("Available Slots" + availablaSlots2);

        bookingService.cancel("Vaibhava","Koramangala",Weights,6,7);
    }

}
