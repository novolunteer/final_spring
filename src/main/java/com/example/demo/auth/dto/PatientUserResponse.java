package com.example.demo.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PatientUserResponse {
    private Integer patientId;
    private Integer userId;
    private Integer role;
    private String rrn;
    private String name;
    private String phone;
    private String address;
    private String gender;
    private String bloodType;
    private Float height;
    private Float weight;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private String status;

    public Map<String, Object> getClaims(){
        Map<String, Object> data=new HashMap<>();
        data.put("patientId",patientId);
        data.put("rrn",rrn);
        data.put("name",name);
        data.put("email",email);
        return data;
    }
}
