package com.example.demo.security.security;

import com.example.demo.staff.Staff;
import com.example.demo.user.User;
import com.example.demo.userRole.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class CustomUserDetails implements UserDetails {
    private Integer userId;
    private String email;
    private String password;
    private String status;
    private Integer departmentId;
    private List<GrantedAuthority> authorities;

    public CustomUserDetails(User user, Integer departmentId){
        this.userId = user.getUserId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.status = user.getStatus();
        this.departmentId = departmentId;

        this.authorities = user.getUserRoles().stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + r.getRole().getRoleName()))
                .toList();
    }
    public CustomUserDetails(Integer userId, String email,
                             String status, Integer departmentId,
                             List<GrantedAuthority> authorities){
        this.userId = userId;
        this.email = email;
        this.status = status;
        this.departmentId = departmentId;
        this.authorities = authorities;
    }

    public Integer getDepartmentId(){
        return departmentId;
    }

    public Integer getUserId(){
        return userId;
    }

    public String getStatus(){
        return status;
    }

    //사용자 권한을 Collection으로 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }
    @Override
    public String getPassword() {
        return password;
    }
    @Override
    public String getUsername() {
        return email;
    }

    public Map<String,Object> getClaims(){ //JWT 관련
        Map<String,Object> dataMap=new HashMap<>();
        dataMap.put("userId", userId);
        dataMap.put("email", email);
        dataMap.put("status", status);
        dataMap.put("roles", authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(r -> r.replace("ROLE_", ""))
                .toList());
        if (departmentId != null){
            dataMap.put("departmentId", departmentId);
        }
        return dataMap;
    }
}
