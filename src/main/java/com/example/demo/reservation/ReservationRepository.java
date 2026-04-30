package com.example.demo.reservation;

import com.example.demo.department.Department;
import com.example.demo.patient.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<Reservation> findByStatusAndPatient_NameContaining(ReservationStatus reservationStatus,
                                                            String name,
                                                            Pageable pageable);
    Page<Reservation> findByStatusAndDepartmentAndPatient_NameContaining(ReservationStatus reservationStatus,
                                                                         Department department,
                                                                         String name,
                                                                         Pageable pageable);

    @Query("""
        select distinct r
        from Reservation r
        where r.patient = :patient
        and (
                :status is null or 
                    r.status = :status
            )
    """)
    Page<Reservation> findMyReservations(Patient patient, ReservationStatus status, Pageable pageable);

}
