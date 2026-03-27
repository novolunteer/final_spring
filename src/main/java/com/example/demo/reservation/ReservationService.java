package com.example.demo.reservation;

import com.example.demo.patient.Patient;
import com.example.demo.patient.PatientRepository;
import com.example.demo.reception.ReceptionService;
import com.example.demo.security.security.CustomUserDetails;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final ReceptionService receptionService;

    public Integer reservationReceived(ReservationDto reservationDto,
                                       CustomUserDetails customUserDetails){

        //Integer userId=customUserDetails.getUserId();
        Integer userId=4;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Not exist"));
        Patient patient = patientRepository.findByUser(user);

        Reservation reservation = reservationDto.toEntity(patient);
        reservation.setStatus("RECEIVED");
        reservation.setSymptom(reservationDto.getSymptom());
        reservation.setPreferredDate(reservationDto.getPreferredDate());
        if(reservationDto.getDoctorId()!=null){
            Staff staff=staffRepository.findByStaffId(reservationDto.getDoctorId());
            reservation.setStaff(staff);
        }

        reservationRepository.save(reservation);

        return reservation.getReservationId();
    }

    public Integer reservationConfirmed(ReservationDto reservationDto){
        Reservation reservation=reservationRepository.findById(reservationDto.getReservationId())
                .orElseThrow(() -> new RuntimeException("Not exist"));

        Staff staff=staffRepository.findById(reservationDto.getDoctorId())
                .orElseThrow(() -> new RuntimeException("Not exist"));
        reservation.setStaff(staff);
        reservation.setReservationDate(reservationDto.getReservationDate());
        reservation.setStatus("CONFIRMED");

        reservationRepository.save(reservation);
        receptionService.receptionInsert(reservation);

        return reservationDto.getReservationId();
    }
}
