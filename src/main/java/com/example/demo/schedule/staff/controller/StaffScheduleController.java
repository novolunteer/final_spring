package com.example.demo.schedule.staff.controller;

import com.example.demo.schedule.staff.dto.StaffScheduleDto;
import com.example.demo.schedule.staff.service.StaffScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/staff_schedule")
public class StaffScheduleController {
    private final StaffScheduleService staffScheduleService;

    @PostMapping("/register")
    public Integer register(@RequestBody StaffScheduleDto dto){
        return staffScheduleService.register(dto);
    }

    @GetMapping("/list")
    public List<StaffScheduleDto> selectAll(){
        return staffScheduleService.selectAll();
    }

    @GetMapping("/{scheduleId}")
    public StaffScheduleDto selectOne(@PathVariable Integer scheduleId){
        return staffScheduleService.selectOne(scheduleId);
    }

    @PutMapping("/{scheduleId}")
    public StaffScheduleDto update(@PathVariable Integer scheduleId,
                                   @RequestBody StaffScheduleDto dto){
        return staffScheduleService.update(scheduleId, dto);
    }

    @DeleteMapping("/{scheduleId}")
    public String delete(@PathVariable Integer scheduleId){
        staffScheduleService.delete(scheduleId);
        return "삭제완료";
    }
}
