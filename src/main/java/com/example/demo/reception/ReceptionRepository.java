package com.example.demo.reception;

import com.example.demo.reception.dto.ReceptionResponse;
import com.example.demo.reservation.Reservation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.parameters.P;

import java.time.LocalDateTime;
import java.util.List;

public interface ReceptionRepository extends JpaRepository<Reception,Integer> {
    @Query("""
    SELECT r FROM Reception r
    JOIN r.reservation v
    JOIN v.slot s
    JOIN v.patient p
    WHERE s.startTime BETWEEN :start AND :end
    AND (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%'))
    ORDER BY s.startTime ASC
    """)
    Page<Reception> findTodayReception(
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("name") String name,
            Pageable pageable
    );

    @Query("""
    SELECT r FROM Reception r
    JOIN r.reservation v
    JOIN v.slot s
    JOIN v.patient p
    WHERE s.startTime BETWEEN :start AND :end
    AND r.status = :status
    AND (:name IS NULL OR p.name LIKE CONCAT('%', :name, '%'))
    ORDER BY s.startTime ASC
    """)
    Page<Reception> findTodayReception(@Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("status") ReceptionStatus status,
                                       @Param("name") String name,
                                       Pageable pageable);

    Reception findByReservation(Reservation reservation);

    @Query("""
        SELECT r FROM Reception r
        JOIN r.reservation v
        JOIN v.staff s
        JOIN v.slot sl
        WHERE r.receptionTime BETWEEN :start AND :end
        AND s.staffId = :doctorId
        AND (
            (:status IS NOT NULL AND r.status = :status)
            OR
            (:status IS NULL AND r.status <> 'PENDING')
        )
        ORDER BY sl.startTime ASC
        """)
    Page<Reception> findTodayReceptionWaiting(@Param("start") LocalDateTime start,
                                               @Param("end") LocalDateTime end,
                                               ReceptionStatus status,
                                               Integer doctorId,
                                               Pageable pageable);
}
