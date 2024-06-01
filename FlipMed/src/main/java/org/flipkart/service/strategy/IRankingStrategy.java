package org.flipkart.service.strategy;

import org.flipkart.entity.Doctor;
import java.util.List;

public interface IRankingStrategy {
    List<Doctor> findBySpeciality(String speciality);
}
