package com.example.demo.staff;

import com.example.demo.department.Department;
import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Staff {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long staffId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId")
    private User user;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departmentId")
    private Department department;

    private String position;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "managerId")
    private Staff manager;

    private String name;
    private String phone;
    private String address;
}
