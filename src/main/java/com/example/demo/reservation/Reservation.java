package com.example.demo.reservation;

import com.example.demo.patient.Patient;
import com.example.demo.staff.Staff;
import com.example.demo.user.User;
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
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reservationId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientId")
    private Patient patient;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorId")
    private Staff staff;

    private String symptom;
    private LocalDateTime preferredDate;

    private LocalDateTime reservationDate;
    private String status;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
