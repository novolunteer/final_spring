package com.example.demo.MedicalRecord.dto;

import com.example.demo.MedicalRecord.MedicalRecord;
import com.example.demo.MedicalRecord.MedicalRecordStatus;
import com.example.demo.patient.Patient;
import com.example.demo.staff.Staff;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MedicalRecordDto {
    private Integer medicalRecordId;
    private Integer patientId;
    private Integer staffId;
    private String symptom;
    private String diseaseCode;
    private MedicalRecordStatus medicalRecordStatus;
    private String title;
    private String content;
    private Boolean isSensitive;
    private LocalDateTime createAt;
    private Boolean isFinal;
    private Integer supervisorId;

    public MedicalRecord toEntity(){
        return MedicalRecord.builder()
                .patient(
                        Patient.builder()
                                .patientId(patientId)
                                .build()
                )
                .medicalRecordStatus(medicalRecordStatus)
                .symptom(symptom)
                .title(title)
                .content(content)
                .isSensitive(isSensitive)
                .isFinal(false)
                .staff(Staff.builder()
                        .staffId(staffId)
                        .build()
                )
                .build();
    }
}
