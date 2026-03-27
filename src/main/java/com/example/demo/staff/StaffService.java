package com.example.demo.staff;

import com.example.demo.department.Department;
import com.example.demo.department.DepartmentRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {
    private final StaffRepository staffRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public Integer register(StaffRegisterDto dto){
        User user=null;
        if(dto.getUserId() !=null){
            user = userRepository.findById(dto.getUserId())
                    .orElseThrow(()-> new RuntimeException("해당 유저가 없습니다"));
        }

        Department department=null;
        if(dto.getDepartmentId() !=null){
            department=departmentRepository.findById(dto.getDepartmentId())
                    .orElseThrow(()->new RuntimeException("해당 부서가 없습니다"));
        }

        Staff manager = null;
        if(dto.getManagerId() !=null){
            manager = staffRepository.findById(dto.getManagerId())
                    .orElseThrow(()-> new RuntimeException("해당 담당자가 없습니다"));
        }

        Staff staff= Staff.builder()
                .user(user)
                .department(department)
                .manager(manager)
                .jobType(dto.getJobType())
                .name(dto.getName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .build();
        Staff savedStaff = staffRepository.save(staff);

        return savedStaff.getStaffId();

    }
}
