package com.example.demo.MedicalRecord.dto;

import com.example.demo.MedicalRecord.MedicalRecord;
import com.example.demo.MedicalRecord.MedicalRecordStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MedicalRecordResponse {
    private Integer medicalRecordId;
    private Integer patientId;
    private Integer doctorId;
    private String doctorName;
    private String departmentName;
    private String symptom;
    private String diseaseCode;

    private MedicalRecordStatus medicalRecordStatus;
    private String title;
    private String content;

    private Boolean isSensitive;
    private LocalDateTime createAt;
    private Boolean isFinal;
    private Integer supervisorId;

    public MedicalRecordResponse(MedicalRecord medicalRecord){
        this.medicalRecordId = medicalRecord.getRecordId();
        this.patientId = medicalRecord.getPatient().getPatientId();
        this.doctorId = medicalRecord.getStaff().getStaffId();
        this.doctorName = medicalRecord.getStaff().getName();
        this.departmentName = medicalRecord.getStaff().getDepartment().getDepartmentName();
        this.symptom = medicalRecord.getSymptom();
        this.diseaseCode = medicalRecord.getDiseaseCode();
        this.medicalRecordStatus = medicalRecord.getMedicalRecordStatus();
        this.title = medicalRecord.getTitle();
        this.content = medicalRecord.getContent();
        this.isSensitive = medicalRecord.getIsSensitive();
        this.createAt = medicalRecord.getCreateAt();
        this.isFinal = medicalRecord.getIsFinal();

        if(medicalRecord.getSupervisor()!=null){
            this.supervisorId = medicalRecord.getSupervisor().getStaffId();
        }

    }
}
