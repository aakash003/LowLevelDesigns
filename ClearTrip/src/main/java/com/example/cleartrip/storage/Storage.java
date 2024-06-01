package com.example.cleartrip.storage;

import com.example.cleartrip.model.Activities;
import com.example.cleartrip.model.Booking;
import com.example.cleartrip.model.Place;
import com.example.cleartrip.model.Timings;
import com.example.cleartrip.model.WorkOut;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Storage {

    private Map<String, List<Timings>> placeTimingsMap = new HashMap<>();
    private Map<String, List<Activities>> placeActivitiesMap = new HashMap<>();
    public Map<String, List<WorkOut>> placeWorkoutMap = new HashMap<>();
    Map<String, Booking> bookingMap = new HashMap<>();


    public Map<String, List<WorkOut>> getPlaceWorkoutMap() {
        return placeWorkoutMap;
    }

    public Map<String, List<Activities>> getPlaceActivitiesMap() {
        return placeActivitiesMap;
    }

    public Map<String, List<Timings>> getPlaceTimingsMap() {
        return placeTimingsMap;
    }

    public void insertPlace(Place place) {
        placeTimingsMap.put(place.getCity(), null);
    }


    public void insertPlaceWithTimings(String place, List<Timings> timings) {
        placeTimingsMap.put(place, timings);
    }

    public void insertPlaceWithActivities(Place place, List<Activities> activities) {
        placeActivitiesMap.put(place.getCity(), activities);
    }

    public void insertWorkout(String place, Activities activities, int startTime, int endTime, int slots) {
        List<WorkOut> workOuts = new ArrayList<>();
        if (placeWorkoutMap.containsKey(place)) {
            workOuts = placeWorkoutMap.get(place);
            workOuts.add(new WorkOut(activities, startTime, endTime, slots));
        } else {
            workOuts.add(new WorkOut(activities, startTime, endTime, slots));
            placeWorkoutMap.put(place, workOuts);
        }
        placeWorkoutMap.put(place, workOuts);
    }


    public boolean checkPlaceOperationTimings(String place, int startTime, int endTime) {
        if (placeTimingsMap.containsKey(place)) {
            List<Timings> timingsList = placeTimingsMap.get(place);
            for (Timings timings : timingsList) {
                if (timings.getStartTime() < startTime && timings.getEndTime() <= startTime
                        && timings.getStartTime() >= endTime && timings.getEndTime() <= endTime) {
                    return true;
                }
            }
        }
        return false;
    }


    public void addBooking(String name, Booking booking) {
        bookingMap.put(name, booking);
    }


    public void remove(String name, String place, Activities activities, int start, int end) {
        //Assuming one user booking at a time
        bookingMap.remove(name);
    }
}
