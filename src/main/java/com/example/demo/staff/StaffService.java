package com.example.demo.staff;

import com.example.demo.department.Department;
import com.example.demo.department.DepartmentDto;
import com.example.demo.department.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffService {
    private final StaffRepository staffRepository;
    private final DepartmentRepository departmentRepository;

    public Map<String, Object> getDoctor(DepartmentDto departmentDto) {
        Department department = departmentRepository.findByDepartmentId(departmentDto.getDepartmentId());
        List<StaffDto> list = staffRepository.findByDepartment(department, Sort.by("name"))
                .orElseThrow(() -> new RuntimeException("Not exist"))
                .stream()
                .map(doc -> StaffDto.builder()
                        .staffId(doc.getStaffId())
                        .name(doc.getName())
                        .build())
                .toList();

        return Map.of("content", list);
    }
}
