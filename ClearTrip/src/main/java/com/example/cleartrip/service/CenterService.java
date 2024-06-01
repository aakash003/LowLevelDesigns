package com.example.cleartrip.service;

import com.example.cleartrip.model.Activities;
import com.example.cleartrip.model.Place;
import com.example.cleartrip.model.Timings;
import com.example.cleartrip.storage.Storage;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@NoArgsConstructor
public class CenterService {

    @Autowired
    private Storage storage;

    public void addCenter(String place) {
        storage.insertPlace(new Place(place));
    }

    public void addCenterTimings(String place, List<Timings> timingsList) {
        storage.insertPlaceWithTimings(place,timingsList);
    }
    public void addCenterActivities(String place, List<Activities> activtieslist) {
        storage.insertPlaceWithActivities(new Place(place),activtieslist);
    }
}
