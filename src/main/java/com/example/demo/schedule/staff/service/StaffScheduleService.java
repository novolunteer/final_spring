package com.example.demo.schedule.staff.service;

import com.example.demo.schedule.staff.dto.BulkRegisterResultDto;
import com.example.demo.schedule.staff.dto.BulkStaffScheduleDto;
import com.example.demo.schedule.staff.dto.SkippedScheduleDto;
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

import java.time.LocalDate;
import java.util.ArrayList;
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
                .status(dto.getStatus())
                .build();
    }

    //전체조회
    public List<StaffScheduleDto> selectAll(){
        List<StaffSchedule> result=staffScheduleRepository.findAll();
        return result.stream()
                .map(this::entityToDto)
                .toList();
    }
    private StaffScheduleDto entityToDto(StaffSchedule entity){
        return StaffScheduleDto.builder()
                .scheduleId(entity.getScheduleId())
                .staffId(entity.getStaff().getStaffId())
                .staffName(entity.getStaff().getName())
                .workDate(entity.getWorkDate())
                .scheduleTypeId(entity.getStaffScheduleType().getScheduleTypeId())
                .typeCode(entity.getStaffScheduleType().getTypeCode())
                .typeName(entity.getStaffScheduleType().getTypeName())
                .departmentId(entity.getStaff().getDepartment().getDepartmentId())
                .departmentName(entity.getStaff().getDepartment().getDepartmentName())
                .status(entity.getStatus())
                .build();
    }

    //조회
    public StaffScheduleDto selectOne(Integer scheduleId){
        StaffSchedule staffSchedule= staffScheduleRepository.findById(scheduleId)
                .orElseThrow(()->new EntityNotFoundException("해당 스케줄이 존재하지 않습니다"));
        return entityToDto(staffSchedule);
    }

    //수정
    public StaffScheduleDto update(Integer scheduleId, StaffScheduleDto dto){
        StaffSchedule staffSchedule=staffScheduleRepository.findById(scheduleId)
                .orElseThrow(()->new EntityNotFoundException("해당 스케줄이 존재하지 않습니다"));
        Staff staff= staffRepository.findById(dto.getStaffId())
                .orElseThrow(()->new EntityNotFoundException("해당 직원이 존재하지 않습니다"));

        StaffScheduleType staffScheduleType=staffScheduleTypeRepository.findById(dto.getScheduleTypeId())
                .orElseThrow(()->new EntityNotFoundException("해당 근무유형이 존재하지 않습니다"));
        staffSchedule.setStaff(staff);
        staffSchedule.setWorkDate(dto.getWorkDate());
        staffSchedule.setStaffScheduleType(staffScheduleType);

        return entityToDto(staffSchedule);
    }

    //삭제
    public void delete(Integer scheduleId){
        StaffSchedule staffSchedule=staffScheduleRepository.findById(scheduleId)
                .orElseThrow(()-> new EntityNotFoundException("해당 스케줄이 존재하지 않습니다"));

        staffScheduleRepository.delete(staffSchedule);
    }

    //스케줄상태변경
    public void confirm(Integer scheduleId) {
        StaffSchedule staffSchedule = staffScheduleRepository.findById(scheduleId)
                .orElseThrow(()->new EntityNotFoundException("해당 스케줄이 존재하지 않습니다"));
        staffSchedule.setStatus("CONFIRMED");
    }
    public void bulkConfirm(List<Integer> scheduleIds){
        List<StaffSchedule> schedules= staffScheduleRepository.findAllById(scheduleIds);
        for (StaffSchedule schedule: schedules){
            schedule.setStatus("CONFIRMED");
        }
    }

    //스케줄 일괄등록: 다른직원 같은스케줄 한번에
    public BulkRegisterResultDto bulkRegister(BulkStaffScheduleDto dto){
        if(dto.getStaffIds()==null || dto.getStaffIds().isEmpty()){
            throw new IllegalStateException("직원을 선택해주세요");
        }
        if(dto.getStartDate()==null || dto.getEndDate()==null){
            throw new IllegalStateException("날짜를 선택해주세요");
        }
        if(dto.getStartDate().isAfter(dto.getEndDate())){
            throw new IllegalStateException("시작일이 종료일보다 늦을 수 없습니다");
        }
        StaffScheduleType staffScheduleType=
                staffScheduleTypeRepository.findById(dto.getScheduleTypeId())
                        .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 근무유형입니다"));

        List<Staff> staffList=staffRepository.findAllById(dto.getStaffIds());

        List<SkippedScheduleDto> skippedList= new ArrayList<>();
        int savedCount =0;
        int skippedCount =0;

        LocalDate currentDate = dto.getStartDate();

        while(!currentDate.isAfter(dto.getEndDate())){

            for(Staff staff : staffList) {
                boolean exist = staffScheduleRepository.existsByStaff_StaffIdAndWorkDate(staff.getStaffId(), currentDate);
                if (exist) {
                    skippedCount ++;
                    skippedList.add(
                            SkippedScheduleDto.builder()
                                    .staffId(staff.getStaffId())
                                    .staffName(staff.getName())
                                    .workDate(currentDate)
                                    .reason("이미 스케줄 존재")
                                    .build()
                    );
                    continue;
                }
                StaffSchedule staffSchedule = StaffSchedule.builder()
                        .staff(staff)
                        .workDate(currentDate)
                        .staffScheduleType(staffScheduleType)
                        .status(dto.getStatus() == null || dto.getStatus().isBlank() ? "TEMP" : dto.getStatus()
                        )
                        .build();
                staffScheduleRepository.save(staffSchedule);
                savedCount ++;
            }
            currentDate = currentDate.plusDays(1);
            }
        return BulkRegisterResultDto.builder()
                .savedCount(savedCount)
                .skippedCount(skippedCount)
                .skippedList(skippedList)
                .message(savedCount + "건 등록," + skippedCount + "건 스케줄 중복으로 제외")
                .build();
    }
}
