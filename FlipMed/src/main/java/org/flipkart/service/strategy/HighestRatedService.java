package org.flipkart.service.strategy;

import org.flipkart.entity.Doctor;
import org.flipkart.helper.ServiceFactory;
import org.flipkart.service.UserService;

import java.util.*;

public class HighestRatedService implements IRankingStrategy {
    UserService userService = ServiceFactory.getUserService();

    @Override
    public List<Doctor> findBySpeciality(String speciality) {
        Map<String, Doctor> doctors = userService.getDoctors();
        List<Doctor> doctorList = new ArrayList<>();
        for (Map.Entry<String, Doctor> entry : doctors.entrySet()) {
            Doctor doctor = entry.getValue();
            if (doctor.getSpeciality().equals(speciality)) {
                doctorList.add(doctor);
            }
        }
        doctorList.sort(Comparator.comparing(Doctor::getRating).reversed());
        return doctorList;
    }
}
