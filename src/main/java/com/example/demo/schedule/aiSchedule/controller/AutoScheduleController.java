package com.example.demo.schedule.aiSchedule.controller;

import com.example.demo.schedule.aiSchedule.dto.AiScheduleResultDto;
import com.example.demo.schedule.aiSchedule.dto.AutoScheduleRequestDto;
import com.example.demo.schedule.aiSchedule.service.AutoScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auto-schedule")
@RequiredArgsConstructor
public class AutoScheduleController {

    private final AutoScheduleService autoScheduleService;

    @PostMapping
    public ResponseEntity<AiScheduleResultDto> generateSchedule(
            @RequestBody AutoScheduleRequestDto request) {
        AiScheduleResultDto result = autoScheduleService.generateSchedule(request);
        return ResponseEntity.ok(result);
    }
}
