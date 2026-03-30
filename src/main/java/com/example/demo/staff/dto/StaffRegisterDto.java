package com.example.demo.staff.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffRegisterDto {
    private Integer userId;
    private Integer departmentId;
    private Integer managerId;

    private String position;
    private String name;
    private String phone;
    private String address;
}
