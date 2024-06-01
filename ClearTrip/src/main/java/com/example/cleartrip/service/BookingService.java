package com.example.cleartrip.service;

import com.example.cleartrip.model.Activities;
import com.example.cleartrip.model.Booking;
import com.example.cleartrip.model.Place;
import com.example.cleartrip.model.WorkOut;
import com.example.cleartrip.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class BookingService {

    @Autowired
    private Storage storage;

    public List<String> getWorkOutChart(Activities weights) {
        List<String> result = new ArrayList<>();
        Map<String, List<WorkOut>> placeWorkoutMap = storage.getPlaceWorkoutMap();
        for (var entry : placeWorkoutMap.entrySet()) {
            String place = entry.getKey();
            for (WorkOut workOut : entry.getValue()) {
                if (workOut.getActivities() == weights)
                    result.add(place + "," + workOut.getActivities() + "," + workOut.getStartTime() + "," + workOut.getEndTime() + "," + workOut.getSlots());
            }
        }
        return result;
    }


    public void bookSession(String name, String place, Activities activities, int start, int end) {
        Map<String, List<WorkOut>> placeWorkoutMap = storage.getPlaceWorkoutMap();
        List<WorkOut> workOutList = placeWorkoutMap.get(place);
        for (WorkOut workOut : workOutList) {
            if (workOut.getStartTime() == start && workOut.getEndTime() == end && workOut.getSlots() > 0) {
                int noOfSlot = workOut.getSlots();
                noOfSlot--;
                workOut.setSlots(noOfSlot);
                storage.addBooking(name,new Booking(name, new WorkOut(activities, start, end, 1), new Place(place)));
            }
        }
    }

    public void cancel(String name, String place, Activities activities, int start, int end) {
        Map<String, List<WorkOut>> placeWorkoutMap = storage.getPlaceWorkoutMap();
        List<WorkOut> workOutList = placeWorkoutMap.get(place);
        for (WorkOut workOut : workOutList) {
            if (workOut.getStartTime() == start && workOut.getEndTime() == end && workOut.getSlots() > 0) {
                int noOfSlot = workOut.getSlots();
                noOfSlot++;
                workOut.setSlots(noOfSlot);
                storage.remove(name,place,activities,start,end);
            }
        }
    }
}

