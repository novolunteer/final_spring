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
    private Integer reservationId;
    private Integer patientId;
    private Integer doctorId;
    private LocalDateTime reservationDate;
    private String symptom;
    private LocalDateTime preferredDate;
    private String status;
    private LocalDateTime createdAt;

    public ReservationDto(Reservation reservation){
        reservationId=reservation.getReservationId();
        patientId=reservation.getPatient().getPatientId();
        doctorId=reservation.getStaff().getStaffId();
        reservationDate=reservation.getReservationDate();
        status=reservation.getStatus();
        createdAt=reservation.getCreatedAt();
    }

    public Reservation toEntity(Patient patient, Staff staff){
        return Reservation.builder()
                .reservationId(reservationId)
                .patient(patient)
                .staff(staff)
                .reservationDate(reservationDate)
                .status(status)
                .createdAt(createdAt)
                .build();
    }

    public Reservation toEntity(Patient patient){
        return Reservation.builder()
                .reservationId(reservationId)
                .patient(patient)
                .reservationDate(reservationDate)
                .status(status)
                .createdAt(createdAt)
                .build();
    }
}
