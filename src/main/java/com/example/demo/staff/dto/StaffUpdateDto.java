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
    private Integer roleId;
    private String name;
    private String phone;
    private String address;
    private String isActive;
}
