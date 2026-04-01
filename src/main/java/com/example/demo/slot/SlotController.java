package com.example.demo.slot;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class SlotController {
    private final SlotService slotService;

    @GetMapping("/api/slot/doctor")
    public Map<String,Object> MonthlyList(@RequestParam("monthly") LocalDateTime monthly,
                                          @RequestParam("doctorId") Integer doctorId){
        return Map.of("content",slotService.MonthlyList(monthly,doctorId));
    }
    @GetMapping("/api/slot/department")
    public Map<String,Object> MonthlyListByDepartment(@RequestParam("monthly") LocalDateTime monthly,
                                          @RequestParam("departmentId") Integer departmentId){
        return Map.of("content",slotService.MonthlyListByDepartment(monthly,departmentId));
    }
}
