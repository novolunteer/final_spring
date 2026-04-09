package com.example.demo.schedule.aiSchedule.dto;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AutoScheduleRequestDto {
    private Integer departmentId;
    private LocalDate startDate;
    private LocalDate endDate;

    private Integer dayMinStaff;
    private Integer eveningMinStaff;
    private Integer nightMinStaff;

    private Integer maxConsecutiveNight;
    private Boolean blockNightToday;

    private List<Integer> excludedStarrIds;
}
