package com.example.demo.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MyReceptionResponse {
    private Integer receptionId;
    private String status;
    private Integer doctorId;
    private String doctorName;
    private Integer departmentId;
    private String departmentName;
    private String symptom;
    private LocalDate receptionDate;
}
