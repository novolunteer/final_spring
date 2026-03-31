package com.example.demo.schedule.staff.service;

import com.example.demo.schedule.staff.dto.StaffScheduleDto;
import com.example.demo.schedule.staff.entity.StaffSchedule;
import com.example.demo.schedule.staff.entity.StaffScheduleType;
import com.example.demo.schedule.staff.repository.StaffScheduleRepository;
import com.example.demo.schedule.staff.repository.StaffScheduleTypeRepository;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class StaffScheduleService {
    private final StaffScheduleRepository staffScheduleRepository;
    private final StaffScheduleTypeRepository staffScheduleTypeRepository;
    private final StaffRepository staffRepository;

    //스케줄 등록
    public Integer register(StaffScheduleDto dto){
        Staff staff=staffRepository.findById(dto.getStaffId())
                .orElseThrow(()->new EntityNotFoundException("존재하지 않는 직원입니다"));
        StaffScheduleType staffScheduleType=staffScheduleTypeRepository.findById(dto.getScheduleTypeId())
                .orElseThrow(()->new EntityNotFoundException("존재하지 않는 근무유형입니다"));
        StaffSchedule staffSchedule=dtoToEntity(dto, staff,staffScheduleType);

        return staffScheduleRepository.save(staffSchedule).getScheduleId();
    }
    private StaffSchedule dtoToEntity(StaffScheduleDto dto, Staff staff, StaffScheduleType staffScheduleType){
        return StaffSchedule.builder()
                .staff(staff)
                .workDate(dto.getWorkDate())
                .staffScheduleType(staffScheduleType)
                .build();
    }

    //전체조회
    public List<StaffScheduleDto> selectAll(){
        List<StaffSchedule> result=staffScheduleRepository.f
    }
}
