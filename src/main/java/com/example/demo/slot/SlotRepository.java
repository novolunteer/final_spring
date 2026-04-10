package com.example.demo.slot;


import com.example.demo.department.Department;
import com.example.demo.staff.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot,Integer> {
    Optional<Slot> findByStartTime(LocalDateTime startTime);

    List<Slot> findAllByStartTimeBetweenAndStaff(LocalDateTime start,
                                          LocalDateTime end,
                                          Staff staff);

    List<Slot> findAllByStartTimeBetweenAndDepartment(LocalDateTime startTimeAfter, LocalDateTime startTimeBefore, Department department);

    Optional<Slot> findByStartTimeAndStaff(LocalDateTime startTime, Staff staff);
}
