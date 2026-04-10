package com.example.demo.audit;

import com.example.demo.medicalRecord.MedicalRecord;
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
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recordId")
    private MedicalRecord medicalRecord;

    @Enumerated(EnumType.STRING)
    private LogStatus logStatus;
    private String reason;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
