package com.example.demo.user;

import com.example.demo.userRole.UserRole;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class UserDto{
    private Long userId;
    private String email;
    private String password;
    private LocalDateTime createdAt;
    private String status;
    private List<String> roles=new ArrayList<>();

    public UserDto(User user){
        this.userId= user.getUserId();
        this.email=user.getEmail();
        this.password=user.getPassword();
        this.createdAt=user.getCreatedAt();
        this.status= user.getStatus();
        this.roles=user.getUserRoles().stream().map(r -> r.getRole().getRoleName()).toList();
    }

    public Map<String,Object> getClaims(){ //JWT 관련
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put("userId", userId);
        dataMap.put("email", email);
        dataMap.put("createdAt", createdAt);
        dataMap.put("status", status);
        dataMap.put("roles",roles);
        return dataMap;
    }
}
