package com.example.demo.reservation;

import com.example.demo.department.Department;
import com.example.demo.reception.Reception;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation,Integer> {
    @Query("""
    SELECT r FROM Reservation r
    JOIN FETCH r.patient p
    JOIN FETCH r.staff f
    JOIN FETCH r.slot s
    """)
    List<Reservation> findAllWithPatientAndStaff(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<Reservation> findByStatus(ReservationStatus reservationStatus);
    List<Reservation> findByStatusAndDepartment(ReservationStatus reservationStatus, Department department);
}
