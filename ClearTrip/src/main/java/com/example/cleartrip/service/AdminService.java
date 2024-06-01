package com.example.cleartrip.service;

import com.example.cleartrip.model.Activities;
import com.example.cleartrip.model.Place;
import com.example.cleartrip.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    @Autowired
    private Storage storage;


    public void addWorkout(String place, Activities weights, int startTime, int endTime, int slots) {
       // if (checkOperationTimings(place, startTime, endTime))
            storage.insertWorkout(place, weights, startTime, endTime, slots);
    }


    private boolean checkOperationTimings(String place, int startTime, int endTime) {
        return storage.checkPlaceOperationTimings(place,startTime,endTime);
    }
}
