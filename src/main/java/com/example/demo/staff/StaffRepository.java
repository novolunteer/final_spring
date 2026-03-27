package com.example.demo.staff;

import com.example.demo.department.Department;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff,Integer> {
    Optional<List<Staff>> findByDepartment(Department department, Sort sort);
    Staff findByStaffId(Integer staffId);
}
