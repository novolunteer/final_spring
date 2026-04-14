package com.example.demo.chatbot;

import com.example.demo.chatbot.dto.response.*;
import com.example.demo.department.Department;
import com.example.demo.department.DepartmentRepository;
import com.example.demo.slot.Slot;
import com.example.demo.slot.SlotRepository;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Stack;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final DepartmentRepository departmentRepository;
    private final StaffRepository staffRepository;
    private final SlotRepository slotRepository;

    public ChatbotDepartmentResponse getDepartmentList() {
        List<ChatbotDepartmentDto> departmentList = departmentRepository.findByStatus("Y")
                .stream().map(d -> ChatbotDepartmentDto.builder()
                        .department(d.getDepartmentName())
                        .build()).toList();

        return ChatbotDepartmentResponse.builder().departments(departmentList).build();
    }

    public ChatbotDepartmentDto getDepartmentInfo(String departmentName){
        Department department=departmentRepository.findByDepartmentName(departmentName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 진료과명입니다."));

        return ChatbotDepartmentDto.builder().department(department.getDepartmentName())
                .location(department.getLocation()).build();
    }

    public ChatbotDoctorResponse getDoctorsInDepartment(String departmentName){
        Department department=departmentRepository.findByDepartmentName(departmentName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 진료과명입니다."));

        List<Staff> staffList=staffRepository.findDoctorsByDepartment(department)
                .orElseThrow(() -> new RuntimeException("해당 진료과에 의사가 존재하지 않습니다."));

        List<ChatbotDoctorDto> doctors=staffList.stream().map(s -> ChatbotDoctorDto.builder()
                .name(s.getName()).build()).toList();

        return ChatbotDoctorResponse.builder().department(departmentName).doctors(doctors).build();
    }

//    public ChatbotReservationResponse getReservationStatus(String departmentName, String date, String startDate, String endDate){
//        Department department=departmentRepository.findByDepartmentName(departmentName)
//                .orElseThrow(() -> new RuntimeException("존재하지 않는 진료과명입니다."));
//
//        if (date != null && !date.isEmpty()){
//            LocalDate localDate=LocalDate.parse(date);
//            LocalDateTime start=localDate.atStartOfDay();
//            LocalDateTime end=localDate.plusDays(1).atStartOfDay();
//            List<Slot> slots=slotRepository.findByDepartmentAndStartTimeGreaterThanEqualAndStartTimeLessThan(
//                    department, start, end);
//
//            Integer totalCount=0;
//            Integer currentCount=0;
//            Integer availableCount=0;
//
//            //스케쥴 아직 안 나왔을 때(스케쥴 테이블이랑 예약 테이블 조회)
//            if (slots == null || slots.isEmpty()){
//
//            }
//        }
//    }
}
