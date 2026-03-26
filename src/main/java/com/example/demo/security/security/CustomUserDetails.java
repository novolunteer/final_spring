package com.example.demo.security.security;

import com.example.demo.user.User;
import com.example.demo.userRole.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class CustomUserDetails implements UserDetails {
    private final User user;
    public CustomUserDetails(User user){
        this.user=user;
    }

    public Long getUserId(){
        return user.getUserId();
    }

    public String getStatus(){
        return user.getStatus();
    }

    //사용자 권한을 Collection으로 반환
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Collection<GrantedAuthority> authorities=new ArrayList<>();
        for (UserRole role: user.getUserRoles()){
            String roleName=role.getRole().getRoleName();
            authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        }

        return authorities;
    }
    @Override
    public String getPassword() {
        return user.getPassword();
    }
    @Override
    public String getUsername() {
        return user.getEmail();
    }
}
