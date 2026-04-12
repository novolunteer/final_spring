package com.example.demo.schedule.aiSchedule.controller;

import com.example.demo.schedule.aiSchedule.dto.DepartmentSchedulePolicyDto;
import com.example.demo.schedule.aiSchedule.service.SchedulePolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/schedule-policy")
@RequiredArgsConstructor
public class SchedulePolicyController {
    private final SchedulePolicyService schedulePolicyService;

    // 전체 목록 조회
    @GetMapping("/list")
    public ResponseEntity<List<DepartmentSchedulePolicyDto>> getAllPolicies() {
        return ResponseEntity.ok(schedulePolicyService.getAllPolicies());
    }

    // 단건 조회
    @GetMapping("/{departmentId}")
    public ResponseEntity<DepartmentSchedulePolicyDto> getPolicy(@PathVariable Integer departmentId) {
        return ResponseEntity.ok(schedulePolicyService.getPolicyByDepartment(departmentId));
    }

    // 등록
    @PostMapping
    public ResponseEntity<DepartmentSchedulePolicyDto> createPolicy(@RequestBody DepartmentSchedulePolicyDto dto) {
        return ResponseEntity.ok(schedulePolicyService.createPolicy(dto));
    }

    // 수정
    @PutMapping("/{departmentId}")
    public ResponseEntity<DepartmentSchedulePolicyDto> updatePolicy(
            @PathVariable Integer departmentId,
            @RequestBody DepartmentSchedulePolicyDto dto) {
        return ResponseEntity.ok(schedulePolicyService.updatePolicy(departmentId, dto));
    }

    // 비활성화
    @DeleteMapping("/{departmentId}")
    public ResponseEntity<Void> deactivatePolicy(@PathVariable Integer departmentId) {
        schedulePolicyService.deactivatePolicy(departmentId);
        return ResponseEntity.noContent().build();
    }
}
