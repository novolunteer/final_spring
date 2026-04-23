package com.example.demo.staff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffBulkUploadRequestDto {
    private Integer userId;
    private String name;
    private String dept_name;
    private String position;
    private String phone;
    private String address;
    private Integer managerId;
    private String isActive;
}
