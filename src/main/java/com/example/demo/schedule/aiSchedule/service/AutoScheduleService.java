package com.example.demo.schedule.aiSchedule.service;

import com.example.demo.schedule.aiSchedule.dto.*;
import com.example.demo.schedule.staff.entity.StaffSchedule;
import com.example.demo.schedule.staff.entity.StaffScheduleType;
import com.example.demo.schedule.staff.repository.StaffScheduleRepository;
import com.example.demo.schedule.staff.repository.StaffScheduleTypeRepository;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AutoScheduleService {

    private final SchedulePolicyService schedulePolicyService;
    private final ConditionParseService conditionParseService;
    private final StaffRepository staffRepository;
    private final StaffScheduleRepository staffScheduleRepository;
    private final StaffScheduleTypeRepository staffScheduleTypeRepository;
    private final RestTemplate restTemplate;

    @Value("${ai.schedule.url}")
    private String aiScheduleUrl;

    @Transactional
    public AiScheduleResultDto generateSchedule(AutoScheduleRequestDto request) {

        // 1. 부서 정책 로드 (근무유형, 최소인원, 제한 규칙)
        DepartmentSchedulePolicyDto policy =
                schedulePolicyService.getPolicyByDepartment(request.getDepartmentId());

        // 2. 해당 부서 직원 목록 로드
        List<Staff> staffList =
                staffRepository.findByDepartmentDepartmentId(request.getDepartmentId());

        // 3. 자연어 추가조건 파싱 (ex: "홍길동 5/5 OFF")
        ConditionParseResultDto parsed =
                conditionParseService.parse(request.getDepartmentId(), request.getExtraCondition());

        // 4. AI에 넘길 입력 DTO 조립
        AiScheduleInputDto input = buildInput(request, policy, staffList, parsed);

        // 5. Python AI 서버 호출
        AiScheduleResultDto result = callAiServer(input);

        // 6. 파싱 경고가 있으면 결과 경고에 합치기
        if (parsed.getWarnings() != null) {
            result.getWarnings().addAll(parsed.getWarnings());
        }

        // 7. AI가 생성한 스케줄 DB 저장
        List<String> validationErrors = saveSchedules(result.getAssignments());
        result.getValidationErrors().addAll(validationErrors);

        return result;
    }

    // ── 내부 메서드 ──────────────────────────────────────────────────────────────

    private AiScheduleInputDto buildInput(AutoScheduleRequestDto request,
                                          DepartmentSchedulePolicyDto policy,
                                          List<Staff> staffList,
                                          ConditionParseResultDto parsed) {

        // Staff 엔티티 → Python이 읽을 수 있는 Map 형태로 변환
        List<Map<String, Object>> staffMapList = staffList.stream()
                .map(s -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("staffId", s.getStaffId());
                    map.put("staffName", s.getName());
                    return map;
                })
                .toList();

        // 정책 필드 → AI 프롬프트용 한국어 규칙 문자열 리스트로 변환
        List<String> rules = buildRules(policy);

        return AiScheduleInputDto.builder()
                .departmentId(policy.getDepartmentId())
                .departmentName(policy.getDepartmentName())
                .jobType(policy.getJobType())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .shiftTypes(policy.getShiftTypes())
                .minStaffMap(policy.getMinStaffMap())
                .staffList(staffMapList)
                .manualConditionList(parsed.getManualConditionList())
                .rules(rules)
                .build();
    }

    // 정책 DTO의 Boolean/Integer 필드를 AI가 이해할 수 있는 규칙 문장으로 변환
    private List<String> buildRules(DepartmentSchedulePolicyDto policy) {
        List<String> rules = new ArrayList<>();

        if (policy.getMaxConsecutiveNight() != null) {
            rules.add("연속 야간(NIGHT) 근무 최대 " + policy.getMaxConsecutiveNight() + "일");
        }
        if (Boolean.TRUE.equals(policy.getBlockNightToDay())) {
            rules.add("야간(NIGHT) 다음날 데이(DAY) 배정 금지");
        }
        if (Boolean.TRUE.equals(policy.getBlockNightToEvening())) {
            rules.add("야간(NIGHT) 다음날 이브닝(EVENING) 배정 금지");
        }
        if (policy.getMaxWorkDaysPerWeek() != null) {
            rules.add("주당 최대 근무일 " + policy.getMaxWorkDaysPerWeek() + "일");
        }

        return rules;
    }

    // Python FastAPI 서버로 POST 요청을 보내고 결과를 받아옴
    // Python 응답: { "scheduleList": [...] } → AiScheduleResultDto로 변환
    private AiScheduleResultDto callAiServer(AiScheduleInputDto input) {
        try {
            PythonScheduleResponseDto pythonResponse =
                    restTemplate.postForObject(aiScheduleUrl, input, PythonScheduleResponseDto.class);

            return AiScheduleResultDto.builder()
                    .assignments(pythonResponse.getScheduleList())
                    .unassigned(new ArrayList<>())
                    .warnings(new ArrayList<>())
                    .validationErrors(new ArrayList<>())
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("AI 스케줄 서버 호출 실패: " + e.getMessage());
        }
    }

    // AI 결과 assignments를 StaffSchedule 엔티티로 변환 후 저장
    private List<String> saveSchedules(List<Map<String, Object>> assignments) {
        List<String> errors = new ArrayList<>();

        for (Map<String, Object> assignment : assignments) {
            Integer staffId = (Integer) assignment.get("staffId");
            String workDateStr = (String) assignment.get("workDate");
            String shiftType = (String) assignment.get("shiftType");

            LocalDate workDate = LocalDate.parse(workDateStr);

            // 이미 해당 날짜에 스케줄이 존재하면 저장 스킵
            if (staffScheduleRepository.existsByStaff_StaffIdAndWorkDate(staffId, workDate)) {
                errors.add("중복 스킵: staffId=" + staffId + ", date=" + workDateStr);
                continue;
            }

            Staff staff = staffRepository.findByStaffId(staffId);
            if (staff == null) {
                errors.add("직원 없음: staffId=" + staffId);
                continue;
            }

            StaffScheduleType scheduleType = staffScheduleTypeRepository
                    .findByTypeCode(shiftType)
                    .orElse(null);
            if (scheduleType == null) {
                errors.add("근무유형 없음: " + shiftType);
                continue;
            }

            staffScheduleRepository.save(StaffSchedule.builder()
                    .staff(staff)
                    .workDate(workDate)
                    .staffScheduleType(scheduleType)
                    .status("AI_GENERATED")
                    .build());
        }

        return errors;
    }
}