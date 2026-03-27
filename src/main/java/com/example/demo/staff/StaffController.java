package com.example.demo.staff;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/staff")
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staffService;

    @PostMapping("/register")
    public ResponseEntity<?> registerStaff(@RequestBody StaffRegisterDto dto){
        Integer staffId = staffService.register(dto);
        return ResponseEntity.ok("직원등록완료 staffId="+staffId);
    }
}
