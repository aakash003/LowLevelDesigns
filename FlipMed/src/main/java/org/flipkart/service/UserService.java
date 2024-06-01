package org.flipkart.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.flipkart.entity.Doctor;
import org.flipkart.entity.Patient;
import org.flipkart.entity.Slots;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserService {
    private static final Logger log = LogManager.getLogger(UserService.class);
    private Map<String, Doctor> doctorMap = new HashMap<>();
    private Map<String, Patient> patientMap = new HashMap<>();

    public void registerDoc(String name, String speciality) {
        log.info("Registering Doctor: " + name + " with speciality: " + speciality);
        Doctor doctor = new Doctor(name, speciality);
        doctorMap.put(doctor.getName(), doctor);
        log.info("Registered Doctor successfully ");
    }

    public void markDoctorAvail(String name, List<String> slots) {
        log.info("Marking Doctor available: " + name);
        Doctor doctor = doctorMap.get(name);
        List<Slots> doctorSlots = doctor.getSlots();
        for (Slots slot : doctorSlots) {
            if (slots.contains(slot.getStartTime() + "-" + slot.getEndTime())) {
                slot.setIsAvailable(true);
                doctor.setSlots(doctorSlots);
            }
        }
        log.info("Marked Doctor available successfully");
    }

    public Map<String, Doctor> getDoctors() {
        return doctorMap;
    }

    public void registerPatient(String name) {
        log.info("Registering Patient: " + name);

        if (patientMap.containsKey(name))
            throw new IllegalArgumentException("Patient already exists");

        patientMap.put(name, new Patient(name));

        log.info("Registered Patient successfully");
    }

    public boolean isDoctorAvailable(String doctorName, String slot) {
        Doctor doctor = doctorMap.get(doctorName);
        List<Slots> doctorSlots = doctor.getSlots();
        for (Slots doctorSlot : doctorSlots) {
            if (doctorSlot.getStartTime().equals(slot) && doctorSlot.getIsAvailable()) {
                return true;
            }
        }
        return false;
    }

    public Slots markDoctorUnavailable(String doctorName, String slot) {
        Doctor doctor = doctorMap.get(doctorName);
        List<Slots> doctorSlots = doctor.getSlots();
        for (Slots doctorSlot : doctorSlots) {
            if (doctorSlot.getStartTime().equals(slot) && doctorSlot.getIsAvailable()) {
                doctorSlot.setIsAvailable(false);
                doctor.setSlots(doctorSlots);
                return doctorSlot;
            }
        }
        return null;
    }
}
