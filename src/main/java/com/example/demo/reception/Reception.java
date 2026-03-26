package com.example.demo.reception;

import com.example.demo.patient.Patient;
import com.example.demo.reservation.Reservation;
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
public class Reception {
    @Id
    private Long receptionId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservationId")
    private Reservation reservation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patientId")
    private Patient patient;

    private String status;

    @CreationTimestamp
    private LocalDateTime receptionTime;
}
