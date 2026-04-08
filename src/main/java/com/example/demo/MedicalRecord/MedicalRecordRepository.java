package com.example.demo.MedicalRecord;

import com.example.demo.patient.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,Integer> {
    Page<MedicalRecord> findAllByPatientAndMedicalRecordStatus(Patient patient, MedicalRecordStatus medicalRecordStatus, Pageable pageable);

    List<MedicalRecord> findTop4ByPatient_PatientIdAndMedicalRecordStatusOrderByCreateAtDesc(Integer patientPatientId, MedicalRecordStatus medicalRecordStatus);

    List<MedicalRecord> findByPatient_PatientIdAndCreateAtBetween(Integer patientPatientId, LocalDateTime createAtAfter, LocalDateTime createAtBefore);
}
