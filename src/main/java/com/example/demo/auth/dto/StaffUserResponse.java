package com.example.demo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class StaffUserResponse {
    private Long staffId;
    private Long userId;
    private Long departmentId;
    private String name;
    private String position;
    private Long managerId;
    private String phone;
    private String address;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private String status;

    public Map<String, Object> getClaims(){
        Map<String,Object> data=new HashMap<>();
        data.put("staffId",staffId);
        data.put("departmentId",departmentId);
        data.put("name",name);
        data.put("position",position);
        data.put("email",email);
        return data;
    }
}
