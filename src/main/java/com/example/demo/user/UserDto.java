package com.example.demo.user;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDto {
    private Long userId;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private String status;

    public Map<String,Object> getClaims(){ //JWT 관련
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put("userId", userId);
        dataMap.put("email", email);
        dataMap.put("password", password);
        dataMap.put("createdAt", createdAt);
        dataMap.put("status", status);
        return dataMap;
    }
}
