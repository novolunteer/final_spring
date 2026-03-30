package com.example.demo.slot;

import com.example.demo.staff.Staff;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Slot {
    @Id
    private Integer slotId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctorId")
    private Staff staff;

    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer maxPatient;
    private Integer currentPatient;
}
