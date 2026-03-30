package com.example.demo.reception;

import com.example.demo.reception.dto.ReceptionResponse;
import com.example.demo.reservation.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReceptionRepository extends JpaRepository<Reception,Integer> {
    @Query("""
    SELECT r FROM Reception r
    JOIN FETCH r.reservation v
    WHERE v.reservationDate BETWEEN :start AND :end""")
    List<Reception> findTodayReception(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
    SELECT r FROM Reception r
    JOIN FETCH r.reservation v
    WHERE v.reservationDate BETWEEN :start AND :end
    AND r.status = :status    
    """)
    List<Reception> findTodayPendingReception(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("status") ReceptionStatus status
    );

}
