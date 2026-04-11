package com.example.demo.schedule.aiSchedule.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
//프론트 -> 백 요청
public class AutoScheduleRequestDto {
    private Integer departmentId;
    private LocalDate startDate;
    private LocalDate endDate;
    //추가 조건
    private String extraCondition;
}
