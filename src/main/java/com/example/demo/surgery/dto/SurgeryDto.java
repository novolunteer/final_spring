package com.example.demo.surgery.dto;

import com.example.demo.surgery.SurgeryStatus;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SurgeryDto {
    private Integer surgeryId;
    private Integer doctorId;
    private Integer patientId;
    private LocalDateTime startTime;
    private Integer durationHours;
    private String description;
    private SurgeryStatus status;
}