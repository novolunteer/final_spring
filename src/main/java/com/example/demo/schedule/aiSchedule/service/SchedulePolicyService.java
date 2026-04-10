package com.example.demo.schedule.aiSchedule.service;

import com.example.demo.schedule.aiSchedule.dto.DepartmentSchedulePolicyDto;
import com.example.demo.schedule.aiSchedule.entity.DepartmentSchedulePolicy;
import com.example.demo.schedule.aiSchedule.entity.DepartmentSchedulePolicyShift;
import com.example.demo.schedule.aiSchedule.repository.DepartmentSchedulePolicyRepository;

import com.example.demo.schedule.aiSchedule.repository.DepartmentSchedulePolicyShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SchedulePolicyService {
    private final DepartmentSchedulePolicyRepository departmentSchedulePolicyRepository;
    private final DepartmentSchedulePolicyShiftRepository departmentSchedulePolicyShiftRepository;

    public DepartmentSchedulePolicyDto getPolicyByDepartment(Integer departmentId) {
        DepartmentSchedulePolicy policy = departmentSchedulePolicyRepository
                .findByDepartmentDepartmentId(departmentId)
                .orElseThrow(() -> new RuntimeException("해당 부서의 스케줄 정책이 없습니다"));

        List<DepartmentSchedulePolicyShift> shiftList = departmentSchedulePolicyShiftRepository
                .findByPolicyPolicyId(policy.getPolicyId());

        List<String> shiftTypes = shiftList.stream()
                .filter(shift -> Boolean.TRUE.equals(shift.getIsEnabled()))
                .map(shift -> shift.getScheduleType().getTypeCode())
                .toList();

        Map<String, Integer> minStaffMap = shiftList.stream()
                .filter(shift -> Boolean.TRUE.equals(shift.getIsEnabled()))
                .collect(Collectors.toMap(shift -> shift.getScheduleType().getTypeCode(),
                        DepartmentSchedulePolicyShift::getMinStaff));

        return DepartmentSchedulePolicyDto.builder()
                .departmentId(policy.getDepartment().getDepartmentId())
                .departmentName(policy.getDepartment().getDepartmentName())
                .jobType(policy.getDepartment().getDepartmentCategory())
                .shiftTypes(shiftTypes)
                .minStaffMap(minStaffMap)
                .maxConsecutiveNight(policy.getMaxConsecutiveNight())
                .blockNightToDay(policy.getBlockNightToDay())
                .blockNightToEvening(policy.getBlockNightToEvening())
                .maxWorkDaysPerWeek(policy.getMaxWorkDaysPerWeek())
                .build();
    }
}