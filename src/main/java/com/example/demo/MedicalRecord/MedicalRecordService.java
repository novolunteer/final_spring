package com.example.demo.MedicalRecord;

import com.example.demo.MedicalRecord.dto.MedicalRecordDto;
import com.example.demo.MedicalRecord.dto.MedicalRecordRequest;
import com.example.demo.MedicalRecord.dto.MedicalRecordResponse;
import com.example.demo.patient.Patient;
import com.example.demo.patient.PatientDto;
import com.example.demo.patient.PatientRepository;
import com.example.demo.reception.ReceptionRepository;
import com.example.demo.reception.ReceptionStatus;
import com.example.demo.reception.dto.ReceptionResponse;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        User user=userRepository.findByUserId(userId);
        Staff doctor=staffRepository.findByUser(user);

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
            medicalRecordDto.setSymptom(medicalRecordDto.getSymptom());
        }

        MedicalRecord medicalRecord=medicalRecordDto.toEntity();
        medicalRecordRepository.save(medicalRecord);

        return medicalRecord.getRecordId();
    }

    public List<Map<String, Object>> getDiagnosisSets(Integer patientId) {
        List<MedicalRecord> diagnosisList =
                medicalRecordRepository.findTop4ByPatient_PatientIdAndMedicalRecordStatusOrderByCreateAtDesc(
                        patientId, MedicalRecordStatus.DIAGNOSIS
                );

        List<Map<String, Object>> result = new ArrayList<>();

        for (int i = 0; i < diagnosisList.size(); i++) {

            MedicalRecord diag = diagnosisList.get(i);

            LocalDateTime start = diag.getCreateAt();
            LocalDateTime end = (i == 0)
                    ? LocalDateTime.now()
                    : diagnosisList.get(i - 1).getCreateAt();

            List<MedicalRecord> related =
                    medicalRecordRepository.findByPatient_PatientIdAndCreateAtBetween(
                            patientId, start, end
                    );

            List<Map<String, Object>> records = related.stream()
                    .filter(r -> r.getMedicalRecordStatus() != MedicalRecordStatus.DIAGNOSIS)
                    .map(r -> {
                        Map<String, Object> map = new HashMap<>();
                        map.put("type", r.getMedicalRecordStatus().name());
                        map.put("content", r.getContent());
                        return map;
                    })
                    .toList();

            Map<String, Object> set = new HashMap<>();

            Map<String, Object> diagnosis = new HashMap<>();
            diagnosis.put("date", diag.getCreateAt());
            diagnosis.put("symptom", diag.getSymptom());
            diagnosis.put("content", diag.getContent());

            set.put("diagnosis", diagnosis);
            set.put("records", records);

            result.add(set);
        }

        return result;
    }
}
