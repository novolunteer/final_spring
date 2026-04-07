package com.example.demo.staff;

import com.example.demo.department.DepartmentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class StaffController {
    private final StaffService staffService;

    @GetMapping("/api/doctor")
    public Map<String,Object> getDepartment(@RequestParam Integer departmentId){
        DepartmentDto departmentDto = new DepartmentDto();
        departmentDto.setDepartmentId(departmentId);
        return staffService.getDoctor(departmentDto);
    }
}
