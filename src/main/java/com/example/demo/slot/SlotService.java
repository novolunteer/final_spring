package com.example.demo.slot;

import com.example.demo.department.DepartmentRepository;
import com.example.demo.slot.dto.SlotDayResponse;
import com.example.demo.slot.dto.SlotDto;
import com.example.demo.slot.dto.SlotResponse;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class SlotService {
    private final SlotRepository slotRepository;
    private final DepartmentRepository departmentRepository;
    private final StaffRepository staffRepository;

    public List<SlotDayResponse> MonthlyList(LocalDateTime monthly, Integer doctorId){
        Staff doctor = staffRepository.findByStaffId(doctorId);

        LocalDateTime start = monthly.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = monthly.withDayOfMonth(monthly.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        List<Slot> slots = slotRepository.findAllByStartTimeBetweenAndStaff(start, end, doctor);

        Map<LocalDate, List<Slot>> grouped = slots.stream()
                .collect(Collectors.groupingBy(slot -> slot.getStartTime().toLocalDate()));

        List<SlotDayResponse> result = new ArrayList<>();

        LocalDate first = monthly.toLocalDate().withDayOfMonth(1);
        LocalDate last = monthly.toLocalDate().withDayOfMonth(monthly.toLocalDate().lengthOfMonth());

        for (LocalDate d = first; !d.isAfter(last); d = d.plusDays(1)) {

            List<Slot> daySlots = grouped.getOrDefault(d, new ArrayList<>());

            int totalCapacity = 0;
            boolean available = false;

            for (int hour = 9; hour <= 18; hour++) {
                final int h = hour;

                List<Slot> hourSlots = daySlots.stream()
                        .filter(s -> s.getStartTime().getHour() == h)
                        .toList();

                int hourCapacity;
                if (hourSlots.isEmpty()) {
                    hourCapacity = 5; // 슬롯 없으면 기본 5명
                } else {
                    hourCapacity = hourSlots.stream()
                            .mapToInt(s -> s.getMaxPatient() - s.getCurrentPatient())
                            .sum();
                }
                totalCapacity += hourCapacity;

                if (hourCapacity > 0) {
                    available = true;
                }
            }

            result.add(SlotDayResponse.builder()
                    .date(d.toString())
                    .totalCapacity(totalCapacity)
                    .available(available)
                    .build());
        }
        return result;
    }

    public List<SlotDayResponse> MonthlyListByDepartment(LocalDateTime monthly, Integer departmentId) {
        // 1. 해당 과의 의사 리스트 조회
        List<Staff> doctors = staffRepository.findAllByDepartment(
                departmentRepository.findByDepartmentId(departmentId)
        );

        // 2. 월 범위 계산
        LocalDateTime start = monthly.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = monthly.withDayOfMonth(monthly.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        // 3. 의사별 슬롯 조회 후 합치기
        List<Slot> allSlots = new ArrayList<>();
        for (Staff doc : doctors) {
            allSlots.addAll(slotRepository.findAllByStartTimeBetweenAndStaff(start, end, doc));
        }

        // 4. 일 단위 그룹핑
        Map<LocalDate, List<Slot>> grouped = allSlots.stream()
                .collect(Collectors.groupingBy(slot -> slot.getStartTime().toLocalDate()));

        // 5. 결과 리스트 생성
        List<SlotDayResponse> result = new ArrayList<>();
        LocalDate firstDay = start.toLocalDate();
        LocalDate lastDay = end.toLocalDate();

        for (LocalDate d = firstDay; !d.isAfter(lastDay); d = d.plusDays(1)) {
            List<Slot> daySlots = grouped.getOrDefault(d, new ArrayList<>());

            int totalCapacity = 0;

            // 하루 9시~18시, 의사별로 계산
            for (Staff doc : doctors) {
                for (int hour = 9; hour <= 18; hour++) {
                    final int h = hour;

                    int capacityForHour = (int) daySlots.stream()
                            .filter(s -> s.getStaff().equals(doc) && s.getStartTime().getHour() == h)
                            .mapToInt(s -> s.getMaxPatient() - s.getCurrentPatient())
                            .sum();

                    // 해당 시간 슬롯이 없으면 기본 5명
                    if (capacityForHour == 0)
                        capacityForHour = 5;

                    totalCapacity += capacityForHour;
                }
            }

            boolean available = totalCapacity > 0;

            result.add(SlotDayResponse.builder()
                    .date(d.toString())
                    .totalCapacity(totalCapacity)
                    .available(available)
                    .build());
        }

        return result;
    }
}
