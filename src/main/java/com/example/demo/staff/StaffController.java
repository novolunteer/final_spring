package com.example.demo.staff;

import com.example.demo.staff.dto.StaffRegisterDto;
import com.example.demo.staff.dto.StaffResponseDto;
import com.example.demo.staff.dto.StaffUpdateDto;
import com.example.demo.department.DepartmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

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

    @GetMapping("/list")
    public ResponseEntity<List<StaffResponseDto>> getAllStaff(){
        List<StaffResponseDto> list=staffService.getAllStaff();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{staffId}")
    public ResponseEntity<StaffResponseDto> getStaffById(@PathVariable Integer staffId){
        StaffResponseDto dto=staffService.getStaffById(staffId);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/update")
    public ResponseEntity<?> updateStaff(@RequestBody StaffUpdateDto dto){
        staffService.updateStaff(dto);
        return ResponseEntity.ok(
                Map.of(
                        "result","success"
                )
        );
    }
    @PutMapping("/active/{staffId}")
    public ResponseEntity<?> updateActive(@PathVariable Integer staffId,
                                          @RequestBody Map<String, String> body) {
        staffService.updateIsActive(staffId, body.get("isActive"));
        return ResponseEntity.ok(Map.of("result", "success"));

    }
    @GetMapping("/doctor")
    public Map<String,Object> getDepartment(@RequestParam Integer departmentId){
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setDepartmentId(departmentId);
        return staffService.getDoctor(departmentDto);
    }
}
