package com.example.demo.reservation;

import com.example.demo.department.Department;
import com.example.demo.department.DepartmentRepository;
import com.example.demo.patient.Patient;
import com.example.demo.patient.PatientRepository;
import com.example.demo.reception.Reception;
import com.example.demo.reception.ReceptionRepository;
import com.example.demo.reception.ReceptionService;
import com.example.demo.reservation.dto.ReservationDto;
import com.example.demo.reservation.dto.ReservationResponse;
import com.example.demo.reservation.dto.ReservationScheduleDto;
import com.example.demo.security.security.CustomUserDetails;
import com.example.demo.slot.Slot;
import com.example.demo.slot.SlotRepository;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final SlotRepository slotRepository;

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

        Department department=departmentRepository.findByDepartmentId(staff.getDepartment().getDepartmentId());

        Slot slot=slotRepository.findByStartTime(reservationDto.getReservationDate())
                .orElseGet(() -> {
                    Slot newSlot=Slot.builder()
                            .currentPatient(0)
                            .maxPatient(3)
                            .startTime(reservationDto.getReservationDate())
                            .staff(staff)
                            .department(department)
                            .build();
                    slotRepository.save(newSlot);
                    return newSlot;
                });
        slot.setCurrentPatient(slot.getCurrentPatient()+1);

        reservation.setStaff(staff);
        reservation.setSlot(slot);
        reservation.setStatus(ReservationStatus.CONFIRMED);

        reservationRepository.save(reservation);
        receptionService.receptionInsert(reservation);

        return reservationDto.getReservationId();
    }

    public Integer reservationPending(ReservationDto reservationDto){
        Reservation reservation=reservationRepository.findById(reservationDto.getReservationId())
                .orElseThrow(() -> new RuntimeException("Not exist"));

        Department department=departmentRepository.findByDepartmentId(reservationDto.getDepartmentId());
        List<Staff> doctors=staffRepository.findDoctorsByDepartment(department)
                .orElseThrow(() -> new RuntimeException("Not exist"));
        Integer maxPatient=doctors.size()*3;

        Slot slot=slotRepository.findByStartTime(reservationDto.getReservationDate())
                .orElseGet(() -> {
                    Slot newSlot=Slot.builder()
                            .currentPatient(0)
                            .maxPatient(maxPatient)
                            .startTime(reservationDto.getReservationDate())
                            .department(department)
                            .build();
                    slotRepository.save(newSlot);
                    return newSlot;
                });
        slot.setCurrentPatient(slot.getCurrentPatient()+1);

        reservation.setSlot(slot);
        reservation.setStatus(ReservationStatus.PENDING);

        reservationRepository.save(reservation);
        receptionService.receptionInsert(reservation);

        return reservationDto.getReservationId();
    }

    public Page<ReservationResponse> reservationList(String name,
                                                     Pageable pageable){
        return reservationRepository.findByStatusAndPatient_NameContaining(ReservationStatus.RECEIVED, name, pageable)
                .map(ReservationResponse::new);
    }

    public Page<ReservationResponse> reservationList(Integer departmentId,
                                                     String name,
                                                     Pageable pageable){
        Department department=departmentRepository.findByDepartmentId(departmentId);

        return reservationRepository.findByStatusAndDepartmentAndPatient_NameContaining(ReservationStatus.RECEIVED,
                                                                                        department,
                                                                                        name,
                                                                                        pageable)
                .map(ReservationResponse::new);
    }

    public Page<ReservationResponse> reservationPendingList(String name,
                                                            Pageable pageable){
        return reservationRepository.findByStatusAndPatient_NameContaining(ReservationStatus.PENDING, name, pageable)
                .map(ReservationResponse::new);
    }

    public Page<ReservationResponse> reservationPendingList(Integer departmentId,
                                                            String name,
                                                            Pageable pageable){
        Department department=departmentRepository.findByDepartmentId(departmentId);

        return reservationRepository.findByStatusAndDepartmentAndPatient_NameContaining(ReservationStatus.PENDING,
                                                                                        department,
                                                                                        name,
                                                                                        pageable)
                .map(ReservationResponse::new);
    }

    public Page<ReservationResponse> reservationConfirmedList(String name,
                                                              Pageable pageable){
        return reservationRepository.findByStatusAndPatient_NameContaining(ReservationStatus.CONFIRMED, name, pageable)
                .map(ReservationResponse::new);
    }

    public Page<ReservationResponse> reservationConfirmedList(Integer departmentId,
                                                              String name,
                                                              Pageable pageable){
        Department department=departmentRepository.findByDepartmentId(departmentId);

        return reservationRepository.findByStatusAndDepartmentAndPatient_NameContaining(ReservationStatus.CONFIRMED,
                                                                                        department,
                                                                                        name,
                                                                                        pageable)
                .map(ReservationResponse::new);
    }

    public Integer reservationCancel(Integer reservationId){
        Reservation reservation=reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Not exist"));

        if(reservation.getSlot()!=null){
            Slot slot=reservation.getSlot();
            slot.setCurrentPatient(slot.getCurrentPatient()-1);
        }

        if(reservation.getStatus()==ReservationStatus.CONFIRMED){
            receptionService.receptionCancel(reservation);
        }

        reservationRepository.delete(reservation);

        return reservationId;
    }

    public Integer reservationUpdate(ReservationDto reservationDto){
        Reservation reservation=reservationRepository.findById(reservationDto.getReservationId())
                .orElseThrow(() -> new RuntimeException("Not exist"));
        Staff staff=staffRepository.findByStaffId(reservationDto.getDoctorId());
        Department department=departmentRepository.findByDepartmentId(reservationDto.getDepartmentId());

        Slot slot=reservation.getSlot();
        slot.setStartTime(reservationDto.getReservationDate());
        slot.setStaff(staff);
        slot.setDepartment(department);

        reservation.setStaff(staff);
        reservation.setSlot(slot);
        reservation.setDepartment(department);

        return reservation.getReservationId();
    }
}
