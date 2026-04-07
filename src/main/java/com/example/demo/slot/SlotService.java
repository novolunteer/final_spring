package com.example.demo.slot;

import com.example.demo.department.DepartmentRepository;
import com.example.demo.slot.dto.SlotDayResponse;
import com.example.demo.slot.dto.SlotDto;
import com.example.demo.slot.dto.SlotResponse;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import jdk.swing.interop.SwingInterOpUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
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

            for (int hour = 9; hour <= 17; hour++) {
                if(hour==13) continue;

                final int h = hour;

                List<Slot> hourSlots = daySlots.stream()
                        .filter(s -> s.getStartTime().getHour() == h)
                        .toList();

                int hourCapacity;
                if (hourSlots.isEmpty()) {
                    hourCapacity = 3; // 슬롯 없으면 기본 5명
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
        // 1. 해당 과 의사 리스트
        List<Staff> doctors = staffRepository.findDoctorsByDepartment( departmentRepository.findByDepartmentId(departmentId) )
                .orElseThrow(() -> new RuntimeException("Not exist"));
        // 2. 월 범위
        LocalDateTime start = monthly.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = monthly.withDayOfMonth(monthly.toLocalDate().lengthOfMonth()) .withHour(23).withMinute(59).withSecond(59);

        // 3. 슬롯 한 번에 조회 (🔥 중요)

        List<Slot> allSlots = slotRepository.findAllByStartTimeBetweenAndDepartment(start, end, departmentRepository.findByDepartmentId(departmentId));

        // 4. (날짜 + 의사 + 시간) 기준으로 미리 Map 만들어두기
        Map<String, Integer> slotMap = new HashMap<>(); for (Slot s : allSlots) {
            String key = s.getStartTime().toLocalDate() + "_" + s.getStaff().getStaffId() + "_" + s.getStartTime().getHour();
            int remain = s.getMaxPatient() - s.getCurrentPatient(); slotMap.merge(key, remain, Integer::sum); }

        // 5. 결과 생성
        List<SlotDayResponse> result = new ArrayList<>();
        LocalDate firstDay = start.toLocalDate(); LocalDate lastDay = end.toLocalDate();
        for (LocalDate d = firstDay; !d.isAfter(lastDay); d = d.plusDays(1)) {
            int totalCapacity = 0;

            for (Staff doc : doctors) {
                for (int hour = 9; hour <= 17; hour++) {
                    if (hour == 13) continue; String key = d + "_" + doc.getStaffId() + "_" + hour;
                    Integer remain = slotMap.get(key);

                    // 슬롯이 없으면 기본 3명
                    if (remain == null) {
                        totalCapacity += 3;
                    } else {
                        totalCapacity += remain;
                    } } }
            result.add(SlotDayResponse.builder() .date(d.toString()) .totalCapacity(totalCapacity) .available(totalCapacity > 0)
                    .build());
        }
        return result;
    }

    public List<SlotDayResponse> MonthlyListByDepartmentInNext(LocalDateTime monthly, Integer departmentId) {

        // 1. 해당 과 의사 리스트
        List<Staff> doctors = staffRepository.findDoctorsByDepartment(
                departmentRepository.findByDepartmentId(departmentId)
        ).orElseThrow(() -> new RuntimeException("Not exist"));

        // 2. 월 범위
        LocalDateTime start = monthly.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        LocalDateTime end = monthly.withDayOfMonth(monthly.toLocalDate().lengthOfMonth())
                .withHour(23).withMinute(59).withSecond(59);

        // 3. 슬롯 한 번에 조회 (🔥 중요)
        List<Slot> allSlots = slotRepository.findAllByStartTimeBetweenAndDepartment(start, end,
                departmentRepository.findByDepartmentId(departmentId));

        // 4. (날짜 + 의사 + 시간) 기준으로 미리 Map 만들어두기
        Map<String, Integer> slotMap = new HashMap<>();

        for (Slot s : allSlots) {
            String key = s.getStartTime().toLocalDate() + "_" +
                    s.getStartTime().getHour();

            int remain = s.getMaxPatient() - s.getCurrentPatient();

            slotMap.merge(key, remain, Integer::sum);
        }

        // 5. 결과 생성
        List<SlotDayResponse> result = new ArrayList<>();

        LocalDate firstDay = start.toLocalDate();
        LocalDate lastDay = end.toLocalDate();

        for (LocalDate d = firstDay; !d.isAfter(lastDay); d = d.plusDays(1)) {

            int totalCapacity = 0;

            for (int hour = 9; hour <= 17; hour++) {
                if (hour == 13) continue;

                String key = d + "_" + hour;

                Integer remain = slotMap.get(key);

                if (remain == null) {
                    // 🔥 슬롯 없으면 → 의사 수 * 3
                    totalCapacity += doctors.size() * 3;
                } else {
                    // 🔥 슬롯 있으면 → (max - current) 합
                    totalCapacity += remain;
                }
            }

            result.add(SlotDayResponse.builder()
                    .date(d.toString())
                    .totalCapacity(totalCapacity)
                    .available(totalCapacity > 0)
                    .build());
        }
        return result;
    }

    public List<SlotResponse> DailyList(LocalDateTime daily,
                                        Integer doctorId){
        List<SlotResponse> result = new ArrayList<>();
        LocalDate date = daily.toLocalDate();

        LocalDateTime start = date.atTime(9, 0);  // 2026-04-02T09:00:00
        LocalDateTime end = date.atTime(17, 0);   // 2026-04-02T18:00:00

        Staff doctor=staffRepository.findByStaffId(doctorId);

        List<Slot> slots = slotRepository.findAllByStartTimeBetweenAndStaff(start, end, doctor);

        Map<Integer, List<Slot>> slotMap = slots.stream()
                .collect(Collectors.groupingBy(s -> s.getStartTime().getHour()));

        for (int hour = 9; hour <= 17; hour++) {
            if(hour==13) continue;
            List<Slot> hourSlots = slotMap.getOrDefault(hour, new ArrayList<>());

            int availableCount;
            int maxPatient = 3; // 기본값

            LocalDateTime slotTime = date.atTime(hour, 0);

            if (hourSlots.isEmpty()) {
                availableCount = maxPatient;
            } else {
                availableCount = hourSlots.stream()
                        .mapToInt(s -> s.getMaxPatient() - s.getCurrentPatient())
                        .sum();
            }
            SlotResponse slotResponse = new SlotResponse();
            slotResponse.setSlotId(hourSlots.isEmpty() ? null : hourSlots.get(0).getSlotId());
            slotResponse.setDoctorId(doctorId);
            slotResponse.setStartTime(slotTime);
            slotResponse.setCapacity(availableCount);
            slotResponse.setAvailable(true);
            slotResponse.setType(SlotStatus.TREATMENT);

            result.add(slotResponse);
        }

        return result;
    }

    public List<SlotResponse> DailyListByDepartment(LocalDateTime daily,
                                                    Integer departmentId){
        // 1. 해당 과의 의사 리스트 조회
        List<Staff> doctors = staffRepository.findDoctorsByDepartment(
                departmentRepository.findByDepartmentId(departmentId)
        ).orElseThrow(() -> new RuntimeException("Department not found"));

        List<SlotResponse> result = new ArrayList<>();
        LocalDate date = daily.toLocalDate();

        // 2. 하루 9시~17시
        for (int hour = 9; hour <= 17; hour++) {
            if(hour == 13) continue; // 점심시간 제외

            LocalDateTime slotTime = date.atTime(hour, 0);

            // 3. 각 의사의 슬롯 조회 및 합산
            int totalCapacity = 0;
            Integer sampleSlotId = null;

            for (Staff doc : doctors) {
                List<Slot> slots = slotRepository.findAllByStartTimeBetweenAndStaff(
                        slotTime,
                        slotTime.plusHours(1).minusSeconds(1),
                        doc
                );

                if (!slots.isEmpty() && sampleSlotId == null) {
                    sampleSlotId = slots.get(0).getSlotId(); // 임의로 slotId 하나 가져오기
                }

                int capacity = slots.stream()
                        .mapToInt(s -> s.getMaxPatient() - s.getCurrentPatient())
                        .sum();

                // 슬롯 없으면 기본 3명
                if (capacity == 0) capacity = 3;
                totalCapacity += capacity;
            }

            // 4. SlotResponse 생성
            SlotResponse slotResponse = new SlotResponse();
            slotResponse.setSlotId(sampleSlotId);
            slotResponse.setStartTime(slotTime);
            slotResponse.setCapacity(totalCapacity);
            slotResponse.setAvailable(totalCapacity > 0);
            slotResponse.setType(SlotStatus.TREATMENT);

            result.add(slotResponse);
        }

        return result;

    }
}
