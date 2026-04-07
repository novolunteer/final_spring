package com.example.demo.billing;

import com.example.demo.MedicalRecord.MedicalRecord;
import com.example.demo.patient.Patient;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Billing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer billingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private MedicalRecord record;

    private Integer totalAmount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BillingStatus status;
}
