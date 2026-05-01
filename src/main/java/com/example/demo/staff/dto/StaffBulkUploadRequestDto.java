package com.example.demo.staff.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffBulkUploadRequestDto {
    private String email;
    private String password;
    private String name;
    private String deptName;
    private String phone;
    private String address;
    private Integer managerId;
    private String isActive;
    private Integer roleId;
}
