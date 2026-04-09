package com.example.demo.service;

import com.example.demo.auth.dto.StaffUserResponse;
import com.example.demo.join.JoinService;
import com.example.demo.user.UserDto;
import com.example.demo.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
@Transactional
@Commit
public class JoinServiceTest {
    @Autowired
    private JoinService joinService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void join(){
        List<Integer> roles=new ArrayList<>();
        roles.add(1);

        StaffUserResponse response=StaffUserResponse.builder()
                .email("jiwon@gmail.com")
                .password(passwordEncoder.encode("1234"))
                .status("Y")
                .roles(roles)
                .departmentId(12)
                .name("Jiwon")
                .phone("010-1999-0920")
                .address("충청남도 천안시 동남구")
                .build();

        UserDto user=joinService.joinTest(response);
        System.out.println(user.getEmail());
    }
}
