package com.example.demo.reception;

import com.example.demo.patient.PatientRepository;
import com.example.demo.reception.dto.ReceptionDto;
import com.example.demo.reception.dto.ReceptionResponse;
import com.example.demo.reservation.Reservation;
import com.example.demo.reservation.ReservationRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class ReceptionService {
    private final ReceptionRepository receptionRepository;
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;

    public Integer receptionInsert(Reservation reservation){
        ReceptionDto receptionDto=ReceptionDto.builder()
                .reservationId(reservation.getReservationId())
                .status(ReceptionStatus.PENDING)
                .build();

        Reception reception=receptionRepository.save(receptionDto.toEntity(reservation));

        return reception.getReceptionId();
    }

    public Map<String,Object> receptionList(){
        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<ReceptionResponse> list=receptionRepository.findTodayReception(start, end)
                .stream()
                .map(ReceptionResponse::new)
                .toList();

        return Map.of("content",list);
    }

    public Map<String,Object> receptionPendingList(){
        LocalDate today = LocalDate.now();

        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();
        List<ReceptionResponse> list=receptionRepository.findTodayPendingReception(start, end, ReceptionStatus.RECEIVED)
                .stream()
                .map(ReceptionResponse::new)
                .toList();
        return Map.of("content",list);
    }

    public Map<String,Object> ReceptionReceived(Integer receptionId){
        Reception reception=receptionRepository.findById(receptionId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        reception.setStatus(ReceptionStatus.RECEIVED);

        receptionRepository.save(reception);

        return Map.of("receptionId",receptionId);
    }

    public Integer receptionConsulting(ReceptionDto receptionDto){
        Integer receptionId=receptionDto.getReceptionId();
        Reception reception=receptionRepository.findById(receptionId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        reception.setStatus(ReceptionStatus.CONSULTING);

        return reception.getReceptionId();
    }

    public Integer receptionCompleted(ReceptionDto receptionDto){
        Integer receptionId=receptionDto.getReceptionId();
        Reception reception=receptionRepository.findById(receptionId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        reception.setStatus(ReceptionStatus.COMPLETED);

        return reception.getReceptionId();
    }

    public Integer receptionCancel(Reservation reservation){
        Reception reception=receptionRepository.findByReservation(reservation);
        receptionRepository.delete(reception);

        return reception.getReceptionId();
    }
}
