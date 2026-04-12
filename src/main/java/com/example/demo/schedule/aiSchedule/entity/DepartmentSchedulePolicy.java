package com.example.demo.schedule.aiSchedule.entity;

import com.example.demo.department.Department;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "department_schedule_policy")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentSchedulePolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer policyId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false, unique = true)
    private Department department;

    private Integer maxConsecutiveNight;
    private Boolean blockNightToDay;
    private Boolean blockNightToEvening;
    private Integer maxWorkDaysPerWeek;

    @Builder.Default
    private Boolean isActive = true;
}
