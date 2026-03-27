package com.example.demo.staff;

import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StaffRepository extends JpaRepository<Staff,Integer> {
    Staff findByStaffId(Integer staffId);
    List<Staff> findByUserIn(List<User> users);
}
