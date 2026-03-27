package com.example.demo.MedicalRecord;

import com.example.demo.patient.Patient;
import com.example.demo.staff.Staff;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class MedicalRecord {
    @Id
    private Integer medicalRecordId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientId")
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorId")
    private Staff staff;
    private String symptom;
    private String diseaseCode;
    private String diagnosis;
    private Boolean isSensitive;

    @CreationTimestamp
    private LocalDateTime createAt;
    private Boolean isFinal;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supervisorId")
    private Staff supervisor;
}
