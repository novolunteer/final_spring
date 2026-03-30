package com.example.demo.reservation;

import com.example.demo.department.Department;
import com.example.demo.department.DepartmentRepository;
import com.example.demo.patient.Patient;
import com.example.demo.patient.PatientRepository;
import com.example.demo.reception.ReceptionService;
import com.example.demo.reservation.dto.ReservationDto;
import com.example.demo.reservation.dto.ReservationResponse;
import com.example.demo.reservation.dto.ReservationScheduleDto;
import com.example.demo.security.security.CustomUserDetails;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final ReceptionService receptionService;
    private final DepartmentRepository departmentRepository;

    public Integer reservationReceived(ReservationDto reservationDto,
                                       CustomUserDetails customUserDetails){

        //Integer userId=customUserDetails.getUserId();
        Integer userId=5;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Not exist"));
        Patient patient = patientRepository.findByUser(user);
        Department department=departmentRepository.findByDepartmentId(reservationDto.getDepartmentId());

        Reservation reservation = reservationDto.toEntity(patient, department);
        reservation.setStatus(ReservationStatus.RECEIVED);
        reservation.setSymptom(reservationDto.getSymptom());

        if(reservationDto.getPreferredDate() != null){
            reservation.setPreferredDate(reservationDto.getPreferredDate());
        }

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
        reservation.setStatus(ReservationStatus.CONFIRMED);

        reservationRepository.save(reservation);
        receptionService.receptionInsert(reservation);

        return reservationDto.getReservationId();
    }

    public List<ReservationResponse> reservationList(){
        return reservationRepository.findByStatus(ReservationStatus.RECEIVED)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> reservationPendingList(){
        return reservationRepository.findByStatus(ReservationStatus.PENDING)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

    public List<ReservationResponse> reservationconfirmedList(){
        return reservationRepository.findByStatus(ReservationStatus.CONFIRMED)
                .stream()
                .map(ReservationResponse::new)
                .toList();
    }

//    public List<ReservationScheduleDto> reservationScheduleList(Integer doctorId){
//        String name=staffRepository.findByStaffId(doctorId).getName();
//
//        ReservationScheduleDto reservationScheduleDto= ReservationScheduleDto.builder()
//                .doctorId(doctorId)
//                .doctorName(name)
//                .build();
//    }
}
