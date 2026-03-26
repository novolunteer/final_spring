package com.example.demo.reservation;

import com.example.demo.patient.Patient;
import com.example.demo.patient.PatientRepository;
import com.example.demo.security.security.CustomUserDetails;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    public Long reservationReceived(ReservationDto reservationDto){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                                                .getContext()
                                                .getAuthentication()
                                                .getPrincipal();

        Long userId = userDetails.getUserId();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Not exist"));
        Patient patient = patientRepository.findByUser(user);

        Reservation reservation = reservationDto.toEntity(patient);
        reservation.setStatus("RECEIVED");

        reservationRepository.save(reservation);

        return reservation.getReservationId();
    }

    public Long reservationConfirmed(ReservationDto reservationDto){
        Reservation reservation=reservationRepository.findById(reservationDto.getReservationId())
                .orElseThrow(() -> new RuntimeException("Not exist"));

        Staff staff=staffRepository.findById(reservationDto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Not exist"));
        reservation.setStaff(staff);
        reservation.setReservationDatetime(reservationDto.getReservationDatetime());

        return reservationDto.getReservationId();
    }
}
