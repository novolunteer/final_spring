package com.example.demo.reception;

import com.example.demo.patient.Patient;
import com.example.demo.reservation.Reservation;
import com.example.demo.staff.Staff;
import jakarta.persistence.FetchType;
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
public class ReceptionDto {
    private Integer receptionId;
    private Integer reservationId;
    private Integer patientId;
    private ReceptionStatus status;
    private LocalDateTime receptionTime;

    public ReceptionDto(Reception reception){
        receptionId=reception.getReceptionId();
        reservationId=reception.getReservation().getReservationId();
        patientId=reception.getPatient().getPatientId();
        status=reception.getStatus();
        receptionTime=reception.getReceptionTime();
    }

    public Reception toEntity(Reservation reservation, Patient patient){
        return Reception.builder()
                .receptionId(reservationId)
                .reservation(reservation)
                .patient(patient)
                .status(status)
                .receptionTime(receptionTime)
                .build();
    }
}
