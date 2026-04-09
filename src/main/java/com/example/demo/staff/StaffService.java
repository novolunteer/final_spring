package com.example.demo.staff;

import com.example.demo.department.Department;
import com.example.demo.department.DepartmentDto;
import com.example.demo.department.DepartmentRepository;
import com.example.demo.staff.dto.StaffRegisterDto;
import com.example.demo.staff.dto.StaffResponseDto;
import com.example.demo.staff.dto.StaffUpdateDto;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffService {
    private final StaffRepository staffRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    //직원등록
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
                .position(dto.getPosition())
                .name(dto.getName())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .isActive(dto.getIsActive() !=null ? dto.getIsActive() : "Y")
                .build();


        Staff savedStaff = staffRepository.save(staff);

        return savedStaff.getStaffId();
    }

    //전체조회
    public List<StaffResponseDto> getAllStaff(){
        List<Staff> staffList = staffRepository.findAll();

        return staffList.stream()
                .map(this::entityToDto)
                .collect(Collectors.toList());
    }

    //Entity->dto
    private StaffResponseDto entityToDto(Staff staff){
        return StaffResponseDto.builder()
                .staffId(staff.getStaffId())
                .name(staff.getName())
                .position(staff.getPosition())
                .phone(staff.getPhone())
                .address(staff.getAddress())
                .isActive(staff.getIsActive())
                .userId(staff.getUser() != null? staff.getUser().getUserId() : null)
                .email(staff.getUser() !=null? staff.getUser().getEmail():null)
                .departmentId(staff.getDepartment() !=null? staff.getDepartment().getDepartmentId():null)
                .departmentName(staff.getDepartment() !=null? staff.getDepartment().getDepartmentName() : null)
                .managerId(staff.getManager() != null? staff.getManager().getStaffId() : null)
                .managerName(staff.getManager() != null? staff.getManager().getName(): null)
                .build();
    }

    public Map<String, Object> getDoctor(DepartmentDto departmentDto) {
        Department department = departmentRepository.findByDepartmentId(departmentDto.getDepartmentId());
        List<StaffDto> list = staffRepository.findDoctorsByDepartment(department)
                .orElseThrow(() -> new RuntimeException("Not exist"))
                .stream()
                .map(doc -> StaffDto.builder()
                        .staffId(doc.getStaffId())
                        .name(doc.getName())
                        .build())
                .toList();
        return Map.of("content", list);
    }
    //상세조회
    public StaffResponseDto getStaffById(Integer staffId){
        Staff staff=staffRepository.findById(staffId)
                .orElseThrow(()->new RuntimeException("해당 직원이 없습니다"));
        return entityToDto(staff);
    }


    //수정
    public void updateStaff(StaffUpdateDto dto) {

        if (dto.getStaffId() == null) {
            throw new RuntimeException("staffId는 필수입니다.");
        }
        if (dto.getUserId() == null) {
            throw new RuntimeException("userId는 필수입니다.");
        }
        if (dto.getDepartmentId() == null) {
            throw new RuntimeException("departmentId는 필수입니다.");
        }

        Staff staff = staffRepository.findById(dto.getStaffId())
                .orElseThrow(() -> new RuntimeException("해당 직원이 없습니다."));

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("해당 유저가 없습니다."));

        Department department = departmentRepository.findById(dto.getDepartmentId())
                .orElseThrow(() -> new RuntimeException("해당 부서가 없습니다."));

        Staff manager = null;
        if (dto.getManagerId() != null) {
            manager = staffRepository.findById(dto.getManagerId())
                    .orElseThrow(() -> new RuntimeException("해당 매니저가 없습니다."));
        }

        staff.setUser(user);
        staff.setDepartment(department);
        staff.setManager(manager);
        staff.setPosition(dto.getPosition());
        staff.setName(dto.getName());
        staff.setPhone(dto.getPhone());
        staff.setAddress(dto.getAddress());
        staff.setIsActive(dto.getIsActive());
    }

    //soft delete
    public void updateIsActive(Integer staffId, String isActive){
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("해당 직원이 없습니다"));

        staff.setIsActive(isActive);
    }
}
