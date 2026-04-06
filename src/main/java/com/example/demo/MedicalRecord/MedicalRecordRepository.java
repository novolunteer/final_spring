package com.example.demo.MedicalRecord;

import com.example.demo.patient.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,Integer> {
    Page<MedicalRecord> findAllByPatientAndMedicalRecordStatus(Patient patient, MedicalRecordStatus medicalRecordStatus, Pageable pageable);
}
