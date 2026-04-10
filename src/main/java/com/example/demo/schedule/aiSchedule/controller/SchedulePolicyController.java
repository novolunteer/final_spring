package com.example.demo.schedule.aiSchedule.controller;

import com.example.demo.schedule.aiSchedule.dto.DepartmentSchedulePolicyDto;
import com.example.demo.schedule.aiSchedule.service.SchedulePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/schedule-policy")
@RequiredArgsConstructor
public class SchedulePolicyController {
    private final SchedulePolicyService schedulePolicyService;

    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentSchedulePolicyDto> getPolicy(@PathVariable Integer departmentId){
        return ResponseEntity.ok(schedulePolicyService.getPolicyByDepartment(departmentId));
    }
}
