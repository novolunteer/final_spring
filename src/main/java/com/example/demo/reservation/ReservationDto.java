package com.example.demo.reservation;

import com.example.demo.patient.Patient;
import com.example.demo.staff.Staff;
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
public class ReservationDto {
    private Long reservationId;
    private Long patientId;
    private Long doctorId;
    private LocalDateTime reservationDatetime;
    private String status;
    private LocalDateTime createdAt;

    public ReservationDto(Reservation reservation){
        reservationId=reservation.getReservationId();
        patientId=reservation.getPatient().getPatientId();
        doctorId=reservation.getStaff().getStaffId();
        reservationDatetime=reservation.getReservationDatetime();
        status=reservation.getStatus();
        createdAt=reservation.getCreatedAt();
    }

    public Reservation toEntity(Patient patient, Staff staff){
        return Reservation.builder()
                .reservationId(reservationId)
                .patient(patient)
                .staff(staff)
                .reservationDatetime(reservationDatetime)
                .status(status)
                .createdAt(createdAt)
                .build();
    }

    public Reservation toEntity(Patient patient){
        return Reservation.builder()
                .reservationId(reservationId)
                .patient(patient)
                .reservationDatetime(reservationDatetime)
                .status(status)
                .createdAt(createdAt)
                .build();
    }
}
