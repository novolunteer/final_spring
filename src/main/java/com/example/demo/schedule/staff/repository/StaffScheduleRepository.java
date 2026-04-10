package com.example.demo.schedule.staff.repository;

import com.example.demo.schedule.staff.entity.StaffSchedule;
import com.example.demo.staff.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface StaffScheduleRepository extends JpaRepository<StaffSchedule, Integer> {
    Optional<StaffSchedule> findByStaff_StaffIdAndWorkDate(Integer staffId, LocalDate workDate);

    boolean existsByStaff_StaffIdAndWorkDate(Integer staffId, LocalDate workDate);

    Optional<StaffSchedule> findByStaff_StaffIdAndWorkDateAndScheduleIdNot(
            Integer staffId,
            LocalDate workDate,
            Integer scheduleId
    );

    boolean existsByStaff_StaffIdAndWorkDateAndScheduleIdNot(
            Integer staffId,
            LocalDate workDate,
            Integer scheduleId
    );

    StaffSchedule findByStaffAndWorkDate(Staff staff, LocalDate workDate);

    List<StaffSchedule> findAllByStaffInAndWorkDateBetween(Collection<Staff> staff, LocalDate workDateAfter, LocalDate workDateBefore);
}
