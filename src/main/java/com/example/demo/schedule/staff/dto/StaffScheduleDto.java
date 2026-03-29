package com.example.demo.schedule.staff.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffScheduleDto {
    private Integer scheduleId;
    private Integer staffId;
    private String staffName;
    private LocalDate workDate;
    private Integer scheduleTypeId;
    private String typeCode;
    private String typeName;
}
