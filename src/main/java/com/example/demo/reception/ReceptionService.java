package com.example.demo.reception;

import com.example.demo.patient.Patient;
import com.example.demo.patient.PatientRepository;
import com.example.demo.reservation.Reservation;
import com.example.demo.reservation.ReservationDto;
import com.example.demo.reservation.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReceptionService {
    private final ReceptionRepository receptionRepository;
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;

    public Long ReceptionReceived(ReservationDto reservationDto){
        Reservation reservation=reservationRepository.findById(reservationDto.getReservationId())
                .orElseThrow(() -> new RuntimeException("Not exist"));
        Patient patient=patientRepository.findById(reservationDto.getPatientId())
                .orElseThrow(() -> new RuntimeException("Not exist"));

        ReceptionDto receptionDto= ReceptionDto.builder()
                .status(ReceptionStatus.RECEIVED)
                .build();
        receptionRepository.save(receptionDto.toEntity(reservation,patient));

        return receptionDto.getReceptionId();
    }

    public Long ReceptionConsulting(ReceptionDto receptionDto){
        Long receptionId=receptionDto.getReceptionId();
        Reception reception=receptionRepository.findById(receptionId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        reception.setStatus(ReceptionStatus.CONSULTING);

        return reception.getReceptionId();
    }

    public Long ReceptionCompleted(ReceptionDto receptionDto){
        Long receptionId=receptionDto.getReceptionId();
        Reception reception=receptionRepository.findById(receptionId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        reception.setStatus(ReceptionStatus.COMPLETED);

        return reception.getReceptionId();
    }
}
