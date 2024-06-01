package org.flipkart.entity;

import java.util.ArrayList;
import java.util.List;

public class Doctor {
    @Override
    public String toString() {
        return "Doctor{" +
                "name='" + name + '\'' +
                ", speciality='" + speciality + '\'' +
                ", rating=" + rating +
                ", slots=" + slots +
                '}';
    }

    private String name;
    private String speciality;
    private int rating;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public List<Slots> getSlots() {
        if (slots == null)
            return new ArrayList<>();
        return slots;
    }

    public void setSlots(List<Slots> slots) {
        this.slots = slots;
    }

    private List<Slots> slots;

    public Doctor(String name, String speciality) {
        this.name = name;
        this.speciality = speciality;
        this.slots = initialiseSlots();
    }

    private List<Slots> initialiseSlots() {
        List<Slots> list = new ArrayList<>();

        for (int i = 9; i < 21; i++) {
            Slots slot = new Slots();
            slot.setSlotId("slot-" + i);
            slot.setIsAvailable(false);
            slot.setStartTime(String.valueOf(i) + ":00");
            slot.setEndTime(String.valueOf(i + 1) + ":00");
            list.add(slot);
        }
        return list;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
