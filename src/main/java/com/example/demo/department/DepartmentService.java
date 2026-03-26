package com.example.demo.department;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Transactional
@RequiredArgsConstructor
public class DepartmentService {
    private final DepartmentRepository departmentRepository;

    public Map<String, Object> getDepartment(){
        List<DepartmentDto> list = departmentRepository.findAll()
                .stream()
                .map(dept -> DepartmentDto.builder()
                        .departmentId(dept.getDepartmentId())
                        .departmentName(dept.getDepartmentName())
                        .build())
                .toList();

        return Map.of("content", list);
    }
}
