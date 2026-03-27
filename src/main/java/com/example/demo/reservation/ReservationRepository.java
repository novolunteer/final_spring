package com.example.demo.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation,Integer> {
    List<Reservation> findAllByReservationDateBetween(
            LocalDateTime start,
            LocalDateTime end
    );
    @Query("""
    SELECT r FROM Reservation r
    JOIN FETCH r.patient p
    JOIN FETCH r.staff s
    WHERE r.reservationDate BETWEEN :start AND :end""")
    List<Reservation> findAllWithPatientAndStaff(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}
