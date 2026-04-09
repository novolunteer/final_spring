package com.example.demo.department;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DepartmentRepository extends JpaRepository<Department, Integer> {
    Department findByDepartmentId(Integer departmentId);

    boolean existsByDepartmentName(String departmentName);
}
