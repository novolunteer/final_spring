package com.example.demo.department;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DepartmentDto {
    private Integer departmentId;
    private String departmentName;
    private String location;
    private String status;
    private String departmentCategory;
}
