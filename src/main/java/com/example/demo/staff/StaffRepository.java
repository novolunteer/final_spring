package com.example.demo.staff;

import com.example.demo.department.Department;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff,Integer> {
    @Query("""
        SELECT s FROM UserRole ur
        JOIN ur.role r
        JOIN ur.user u
        JOIN Staff s ON s.user = u
        WHERE s.department = :dept
        AND r.roleName = 'DOCTOR'
        AND u.status = 'Y'
       ORDER BY r.roleName
    """)
    Optional<List<Staff>> findDoctorsByDepartment(Department dept);
    Staff findByStaffId(Integer staffId);

    List<Staff> findAllByDepartment(Department Department);
}
