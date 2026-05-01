package com.example.demo.patient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class MyInfoResponse {
    private Integer userId;
    private Integer patientId;
    private String name;
    private String email;
    private String rrn;
    private String phone;
    private String address;
    private String gender;
    private String bloodType;
    private Float height;
    private Float weight;

    private List<MySocialAccount> socialAccounts;
}
