package com.example.demo.schedule.staff.repository;

import com.example.demo.schedule.staff.entity.StaffSchedule;
import com.example.demo.staff.Staff;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
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

    List<StaffSchedule> findByStatusAndStaffScheduleType_ScheduleTypeIdAndWorkDateAndStaffIn(String status, Integer typeId, LocalDate date, List<Staff> staff);
    List<StaffSchedule> findByStatusAndStaffScheduleType_ScheduleTypeIdAndWorkDateBetweenAndStaffIn(String status,
                                                                                                       Integer typeId,
                                                                                                       LocalDate start,
                                                                                                       LocalDate end,
                                                                                                       List<Staff> staff);

    StaffSchedule findByStaffAndStatusAndStaffScheduleType_ScheduleTypeIdAndWorkDate(Staff staff, String status,
                                                                                     Integer typeId, LocalDate date);
    List<StaffSchedule> findByStaffAndStatusAndStaffScheduleType_ScheduleTypeIdAndWorkDateBetween(Staff staff, String status,
                                                                                                  Integer typeId, LocalDate start,
                                                                                                  LocalDate end);
}
