package com.example.demo.schedule.aiSchedule.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AiScheduleInputDto {
    private Integer departmentId;
    private String departmentName;
    private String jobType;

    private LocalDate startDate;
    private LocalDate endDate;

    private List<String> shiftTypes;
    private Map<String, Integer> minStaffMap;

    private List<Map<String, Object>> staffList;
    private List<AiManualConditionDto> manualConditionList;
    private List<String> rules;
}
