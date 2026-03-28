package com.example.demo.staff.dto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUpdateDto {
    private Integer staffId;

    private Integer userId;
    private Integer departmentId;
    private Integer managerId;

    private String position;
    private String jobType;
    private String name;
    private String phone;
    private String address;
}
