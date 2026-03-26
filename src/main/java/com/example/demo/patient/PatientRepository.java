package com.example.demo.patient;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByUser(User user);
}
