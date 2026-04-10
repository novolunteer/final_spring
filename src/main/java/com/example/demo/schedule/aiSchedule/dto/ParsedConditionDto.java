package com.example.demo.schedule.aiSchedule.dto;

import lombok.*;

import java.lang.Object;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ParsedConditionDto {
    //extraCondition 리스트
    private List<Map<String, Object>> manualConditionList;
    //파싱 실패, 애매한 조건경고
    private List<String> warnings;
}
