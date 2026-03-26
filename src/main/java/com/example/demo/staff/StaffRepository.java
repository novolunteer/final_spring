package com.example.demo.staff;

import org.springframework.data.jpa.repository.JpaRepository;

public interface StaffRepository extends JpaRepository<Staff,Integer> {
    Staff findByStaffId(Integer staffId);
}
