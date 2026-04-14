package com.example.demo.chatbot;

import com.example.demo.chatbot.dto.request.DoctorScheduleInquiryRequest;
import com.example.demo.chatbot.dto.response.*;
import com.example.demo.department.Department;
import com.example.demo.department.DepartmentRepository;
import com.example.demo.schedule.staff.entity.StaffSchedule;
import com.example.demo.schedule.staff.repository.StaffScheduleRepository;
import com.example.demo.slot.Slot;
import com.example.demo.slot.SlotRepository;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotService {
    private final DepartmentRepository departmentRepository;
    private final StaffRepository staffRepository;
    private final SlotRepository slotRepository;
    private final StaffScheduleRepository scheduleRepository;

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

    public ChatbotReservationResponse getReservationStatus(String departmentName, String date, String startDate, String endDate){
        if (departmentName == null || departmentName.isEmpty()){
            throw new RuntimeException("진료과명이 포함되지 않은 질문입니다.");
        }

        boolean hasDate = date != null && !date.isEmpty();
        boolean hasRange = startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty();

        if (!hasDate && !hasRange) {
            throw new RuntimeException("날짜 정보가 포함되지 않은 질문입니다.");
        }

        if (hasRange && LocalDate.parse(startDate).isAfter(LocalDate.parse(endDate))) {
            throw new RuntimeException("시작일이 종료일보다 늦습니다.");
        }

        Department department=departmentRepository.findByDepartmentName(departmentName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 진료과명입니다."));

        List<Staff> doctors=staffRepository.findDoctorsByDepartment(department)
                .orElseThrow(() -> new RuntimeException("해당 진료과에 의사가 존재하지 않습니다."));

        if (hasDate){
            LocalDate localDate=LocalDate.parse(date);
            LocalDateTime start=localDate.atStartOfDay();
            LocalDateTime end=localDate.plusDays(1).atStartOfDay();

            Integer totalCount=0;
            Integer currentCount=0;
            Integer availableCount=0;
            boolean schedulePublished=true;

            List<StaffSchedule> schedules=scheduleRepository.findByStatusAndStaffScheduleType_ScheduleTypeIdAndWorkDateAndStaffIn(
                    "CONFIRMED", 1, localDate,doctors
            );

            if (schedules == null || schedules.isEmpty()){
                totalCount=(3*8) * doctors.size();

                List<Slot> slots=slotRepository.findByDepartmentAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                        department, start, end);
                if (slots != null && !slots.isEmpty()){
                    for (Slot s:slots){
                        currentCount += s.getCurrentPatient();
                    }

                    availableCount=Math.max(totalCount - currentCount, 0);
                    schedulePublished=false;

                    return ChatbotReservationResponse.builder()
                            .department(departmentName)
                            .date(date)
                            .totalCount(totalCount)
                            .availableCount(availableCount)
                            .schedulePublished(schedulePublished).build();
                }

                availableCount=totalCount;
                schedulePublished=false;

                return ChatbotReservationResponse.builder()
                        .department(departmentName)
                        .date(date)
                        .totalCount(totalCount)
                        .availableCount(availableCount)
                        .schedulePublished(schedulePublished).build();
            }

            totalCount=(3*8)*schedules.size();

            List<Slot> slots=slotRepository.findByDepartmentAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                    department, start, end);

            if (slots != null && !slots.isEmpty()){
                for (Slot s:slots){
                    currentCount += s.getCurrentPatient();
                }

                availableCount=Math.max(totalCount - currentCount, 0);

                return ChatbotReservationResponse.builder()
                        .department(departmentName)
                        .date(date)
                        .totalCount(totalCount)
                        .availableCount(availableCount)
                        .schedulePublished(schedulePublished).build();
            }

            availableCount=totalCount;

            return ChatbotReservationResponse.builder()
                    .department(departmentName)
                    .date(date)
                    .totalCount(totalCount)
                    .availableCount(availableCount)
                    .schedulePublished(schedulePublished).build();
        }

        LocalDate localStart=LocalDate.parse(startDate);
        LocalDateTime start=localStart.atStartOfDay();

        LocalDate localEnd=LocalDate.parse(endDate);
        LocalDateTime end=localEnd.plusDays(1).atStartOfDay();

        Integer totalCount=0;
        Integer currentCount=0;
        Integer availableCount=0;
        boolean schedulePublished=true;
        long days= ChronoUnit.DAYS.between(localStart, localEnd)+1;

        List<StaffSchedule> schedules=
                scheduleRepository.findByStatusAndStaffScheduleType_ScheduleTypeIdAndWorkDateBetweenAndStaffIn(
                "CONFIRMED", 1, localStart, localEnd, doctors
        );

        if (schedules == null || schedules.isEmpty()){
            totalCount=(int) (((3*8)*doctors.size())*days);

            List<Slot> slots=slotRepository.findByDepartmentAndStartTimeGreaterThanEqualAndStartTimeLessThan(department, start, end);
            if (slots != null && !slots.isEmpty()){
                for (Slot s:slots){
                    currentCount += s.getCurrentPatient();
                }

                availableCount=Math.max(totalCount - currentCount, 0);
                schedulePublished=false;

                return ChatbotReservationResponse.builder()
                        .department(departmentName)
                        .totalCount(totalCount)
                        .availableCount(availableCount)
                        .schedulePublished(schedulePublished).build();
            }

            availableCount=totalCount;
            schedulePublished=false;

            return ChatbotReservationResponse.builder()
                    .department(departmentName)
                    .totalCount(totalCount)
                    .availableCount(availableCount)
                    .schedulePublished(schedulePublished).build();
        }

        totalCount=(3*8)*schedules.size();

        Set<LocalDate> confirmDates=schedules.stream()
                .map(StaffSchedule :: getWorkDate)
                .collect(Collectors.toSet());

        boolean hasUnpublish=false;
        for (int i=0; i < days; i++){
            if (!confirmDates.contains(localStart.plusDays(i))){
                hasUnpublish=true;
                break;
            }
        }

        if (hasUnpublish){
            List<Slot> slots=slotRepository.findByDepartmentAndStartTimeGreaterThanEqualAndStartTimeLessThan(department, start, end);
            if (slots != null && !slots.isEmpty()){
                for (Slot s:slots){
                    currentCount += s.getCurrentPatient();
                }

                availableCount=Math.max(totalCount - currentCount, 0);
                schedulePublished=false;

                return ChatbotReservationResponse.builder()
                        .department(departmentName)
                        .totalCount(totalCount)
                        .availableCount(availableCount)
                        .schedulePublished(schedulePublished).build();
            }



            availableCount=totalCount;
            schedulePublished=false;

            return ChatbotReservationResponse.builder()
                    .department(departmentName)
                    .totalCount(totalCount)
                    .availableCount(availableCount)
                    .schedulePublished(schedulePublished).build();
        }

        List<Slot> slots=slotRepository.findByDepartmentAndStartTimeGreaterThanEqualAndStartTimeLessThan(department, start, end);
        if (slots != null && !slots.isEmpty()){
            for (Slot s:slots){
                currentCount += s.getCurrentPatient();
            }

            availableCount=Math.max(totalCount - currentCount, 0);

            return ChatbotReservationResponse.builder()
                    .department(departmentName)
                    .totalCount(totalCount)
                    .availableCount(availableCount)
                    .schedulePublished(schedulePublished).build();
        }



        availableCount=totalCount;

        return ChatbotReservationResponse.builder()
                .department(departmentName)
                .totalCount(totalCount)
                .availableCount(availableCount)
                .schedulePublished(schedulePublished).build();
    }

    public ChatbotDoctorScheduleResponse getDoctorSchedule(DoctorScheduleInquiryRequest request){
        String name= request.getDoctorName();
        if (name == null || name.isEmpty()){
            throw new RuntimeException("의사 이름이 포함되지 않은 질문입니다.");
        }

        String departmentName= request.getDepartment();
        if (departmentName == null || departmentName.isEmpty()){
            throw new RuntimeException("진료과명이 포함되지 않은 질문입니다.");
        }

        boolean hasDate = request.getDate() != null && !request.getDate().isEmpty();
        boolean hasRange = request.getStartDate() != null && !request.getStartDate().isEmpty()
                && request.getEndDate() != null && !request.getEndDate().isEmpty();

        if (!hasDate && !hasRange) {
            throw new RuntimeException("날짜 정보가 포함되지 않은 질문입니다.");
        }

        if (hasRange && LocalDate.parse(request.getStartDate()).isAfter(LocalDate.parse(request.getEndDate()))) {
            throw new RuntimeException("시작일이 종료일보다 늦습니다.");
        }

        Department department=departmentRepository.findByDepartmentName(departmentName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 진료과입니다."));

        Staff doctor=staffRepository.findByDepartmentAndName(department, name)
                .orElseThrow(()->new RuntimeException("존재하지 않는 의사입니다."));

        List<String> times = List.of("09","10","11","12","14","15","16","17");
        boolean published=true;

        if (hasDate){
            LocalDate date=LocalDate.parse(request.getDate());
            LocalDateTime start=date.atStartOfDay();
            LocalDateTime end=date.plusDays(1).atStartOfDay();

            StaffSchedule schedule=scheduleRepository.findByStaffAndStatusAndStaffScheduleType_ScheduleTypeIdAndWorkDate(
                    doctor, "CONFIRMED", 1, date
            );
            if (schedule == null){
                LocalDate today=LocalDate.now();
                if (date == today){
                    return ChatbotDoctorScheduleResponse.builder()
                            .department(departmentName)
                            .doctorName(name)
                            .date(request.getDate())
                            .available(false)
                            .schedules(null)
                            .schedulePublished(published).build();
                }

                List<Slot> slots=slotRepository.findByDepartmentAndStaffAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                        department, doctor, start, end
                );

                if (slots == null || slots.isEmpty()){
                    List<ChatbotDoctorScheduleDto> schedules=new ArrayList<>();

                    for (String s:times){
                        schedules.add(ChatbotDoctorScheduleDto.builder()
                                .date(request.getDate())
                                .time(s + ":00:00").build());
                    }

                    published=false;

                    return ChatbotDoctorScheduleResponse.builder()
                            .department(departmentName)
                            .doctorName(name)
                            .date(request.getDate())
                            .available(true)
                            .schedules(schedules)
                            .schedulePublished(published).build();
                }

                Set<Integer> slotHours = slots.stream()
                        .filter(s -> s.getCurrentPatient() == s.getMaxPatient())
                        .map(s -> s.getStartTime().getHour())
                        .collect(Collectors.toSet());

                List<String> leftTimes = times.stream()
                        .filter(t -> !slotHours.contains(Integer.parseInt(t)))
                        .toList();

                List<ChatbotDoctorScheduleDto> schedules=new ArrayList<>();

                for (String s:leftTimes){
                    schedules.add(ChatbotDoctorScheduleDto.builder()
                            .date(request.getDate())
                            .time(s + ":00:00").build());
                }

                published=false;

                return ChatbotDoctorScheduleResponse.builder()
                        .department(departmentName)
                        .doctorName(name)
                        .date(request.getDate())
                        .available(schedules == null ? false:true)
                        .schedules(schedules)
                        .schedulePublished(published).build();
            }

            List<Slot> slots=slotRepository.findByDepartmentAndStaffAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                    department, doctor, start, end
            );

            if (slots == null || slots.isEmpty()){
                List<ChatbotDoctorScheduleDto> schedules=new ArrayList<>();

                for (String s:times){
                    schedules.add(ChatbotDoctorScheduleDto.builder()
                            .date(request.getDate())
                            .time(s + ":00:00").build());
                }

                return ChatbotDoctorScheduleResponse.builder()
                        .department(departmentName)
                        .doctorName(name)
                        .date(request.getDate())
                        .available(true)
                        .schedules(schedules)
                        .schedulePublished(published).build();
            }

            Set<Integer> slotHours = slots.stream()
                    .filter(s -> s.getCurrentPatient() == s.getMaxPatient())
                    .map(s -> s.getStartTime().getHour())
                    .collect(Collectors.toSet());

            List<String> leftTimes = times.stream()
                    .filter(t -> !slotHours.contains(Integer.parseInt(t)))
                    .toList();

            List<ChatbotDoctorScheduleDto> schedules=new ArrayList<>();

            for (String s:leftTimes){
                schedules.add(ChatbotDoctorScheduleDto.builder()
                        .date(request.getDate())
                        .time(s + ":00:00").build());
            }

            return ChatbotDoctorScheduleResponse.builder()
                    .department(departmentName)
                    .doctorName(name)
                    .date(request.getDate())
                    .available(schedules == null ? false:true)
                    .schedules(schedules)
                    .schedulePublished(published).build();
        }

        LocalDate localStart=LocalDate.parse(request.getStartDate());
        LocalDateTime start=localStart.atStartOfDay();
        LocalDate localEnd=LocalDate.parse(request.getEndDate());
        LocalDateTime end=localEnd.plusDays(1).atStartOfDay();

        long days=ChronoUnit.DAYS.between(localStart, localEnd)+1;

        List<StaffSchedule> schedules=
                scheduleRepository.findByStaffAndStatusAndStaffScheduleType_ScheduleTypeIdAndWorkDateBetween(
                    doctor, "CONFIRMED", 1, localStart, localEnd
        );

        if (schedules == null || schedules.isEmpty()){
            List<Slot> slots=slotRepository.findByDepartmentAndStaffAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                    department, doctor, start, end
            );

            if (slots == null || slots.isEmpty()){
                List<ChatbotDoctorScheduleDto> schedule=new ArrayList<>();

                for (int i=0; i<days; i++){
                    for (String s:times){
                        schedule.add(ChatbotDoctorScheduleDto.builder()
                                .date(String.valueOf(localStart.plusDays(i)))
                                .time(s + ":00:00")
                                .build());
                    }
                }

                published=false;

                return ChatbotDoctorScheduleResponse.builder()
                        .department(departmentName)
                        .doctorName(name)
                        .startDate(request.getStartDate())
                        .endDate(request.getEndDate())
                        .available(true)
                        .schedules(schedule)
                        .schedulePublished(published).build();
            }

            //예약 다 찬 시간
            List<LocalDateTime> slotDates=slots.stream()
                    .filter(s -> s.getCurrentPatient() == s.getMaxPatient())
                    .map(s -> s.getStartTime()).toList();

            List<String> leftDates=new ArrayList<>();
            List<ChatbotDoctorScheduleDto> schedule=new ArrayList<>();

            for (int i=0; i<days; i++){
                for (LocalDateTime t:slotDates){
                    if (t.toLocalDate() == localStart.plusDays(i)) continue;
                    leftDates.add(String.valueOf(localStart.plusDays(i)));
                }
            }

            for (String s:leftDates){
                for (LocalDateTime t:slotDates){
                    for (String r:times){
                        if (String.valueOf(t.getHour()).equals(r)) continue;
                        schedule.add(ChatbotDoctorScheduleDto.builder()
                                .date(s)
                                .time(r + ":00:00")
                                .build());
                    }
                }
            }

            published=false;

            return ChatbotDoctorScheduleResponse.builder()
                    .department(departmentName)
                    .doctorName(name)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .available(schedule == null ? false:true)
                    .schedules(schedule)
                    .schedulePublished(published).build();
        }

        //스케쥴 있는 날
        List<LocalDate> scheduleDates=schedules.stream().map(s -> s.getWorkDate()).toList();

        List<Slot> allSlots=new ArrayList<>();

        for (LocalDate d:scheduleDates){
            LocalDateTime startTime=d.atStartOfDay();
            LocalDateTime endTime=d.plusDays(1).atStartOfDay();
            List<Slot> slots=
                    slotRepository.findByDepartmentAndStaffAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                            department, doctor, startTime, endTime
                    );
            if (slots == null || slots.isEmpty()) continue;

            allSlots.addAll(slots);
        }

        List<LocalDateTime> fullSlot=allSlots.stream()
                .filter(s -> s.getCurrentPatient() == s.getMaxPatient())
                .map(s -> s.getStartTime()).toList();

        List<ChatbotDoctorScheduleDto> schedule=new ArrayList<>();

        for (LocalDate d:scheduleDates){
            for (LocalDateTime f:fullSlot){
                for (String t:times){
                    if (String.valueOf(f.getHour()).equals(t)) continue;
                        schedule.add(ChatbotDoctorScheduleDto.builder()
                                .date(String.valueOf(d))
                                .time(t + ":00:00")
                                .build());
                }
            }
        }

        return ChatbotDoctorScheduleResponse.builder()
                .department(departmentName)
                .doctorName(name)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .available(schedule == null ? false:true)
                .schedules(schedule)
                .schedulePublished(published).build();
    }
}
