package com.example.demo.medicalRecord;

import com.example.demo.medicalRecord.dto.MedicalRecordDto;
import com.example.demo.medicalRecord.dto.MedicalRecordRequest;
import com.example.demo.medicalRecord.dto.MedicalRecordResponse;
import com.example.demo.patient.Patient;
import com.example.demo.patient.PatientDto;
import com.example.demo.patient.PatientRepository;
import com.example.demo.reception.ReceptionRepository;
import com.example.demo.reception.ReceptionStatus;
import com.example.demo.reception.dto.ReceptionResponse;
import com.example.demo.security.security.CustomUserDetails;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final ReceptionRepository receptionRepository;
    private final StaffRepository staffRepository;
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;

    public Page<ReceptionResponse> waitingList(Integer userId,
                                               ReceptionStatus status,
                                               Pageable pageable){
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        User user=userRepository.findByUserId(userId)
                .orElseThrow(()->new RuntimeException("Not exist"));
        Staff doctor=staffRepository.findByUser(user)
                .orElseThrow(()->new RuntimeException("Not exist"));

        return receptionRepository.findTodayReceptionWaiting(start,end,status,doctor.getStaffId(),pageable)
                .map(ReceptionResponse::new);
    }

    public Map<String, Object> patientInfo(Integer patientId){
        Patient patient=patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        return Map.of("patient",new PatientDto(patient));
    }

    public Page<MedicalRecordResponse> medicalRecord(Integer patientId,
                                                     MedicalRecordStatus status,
                                                     Pageable pageable){
        Patient patient=patientRepository.findById(patientId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        return medicalRecordRepository.findAllByPatientAndMedicalRecordStatus(patient, status, pageable)
                .map(MedicalRecordResponse::new);
    }

    public MedicalRecordResponse medicalRecordDetail(Integer recordId,
                                                     String reason,
                                                     CustomUserDetails customUserDetails){
        MedicalRecord medicalRecord=medicalRecordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        return new MedicalRecordResponse(medicalRecord);
    }

    public Integer MedicalRecordInsert(MedicalRecordRequest medicalRecordRequest,
                                       Integer doctorId){
        MedicalRecordDto medicalRecordDto= MedicalRecordDto.builder()
                .patientId(medicalRecordRequest.getPatientId())
                .staffId(doctorId)
                .medicalRecordStatus(medicalRecordRequest.getMedicalRecordStatus())
                .title(medicalRecordRequest.getTitle())
                .content(medicalRecordRequest.getContent())
                .isSensitive(medicalRecordRequest.getIsSensitive())
                .build();
        if(medicalRecordRequest.getSymptom() != null){
            medicalRecordDto.setSymptom(medicalRecordRequest.getSymptom());
        }

        MedicalRecord medicalRecord=medicalRecordDto.toEntity();
        medicalRecordRepository.save(medicalRecord);

        return medicalRecord.getRecordId();
    }
}
