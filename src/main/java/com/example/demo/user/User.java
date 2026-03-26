package com.example.demo.user;

import com.example.demo.userRole.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @CreationTimestamp
    private LocalDateTime createdAt;

    private String status;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    @Builder.Default
    private List<UserRole> userRoles=new ArrayList<>();

    public void addRole(UserRole role){
        userRoles.add(role);
    }

    public void clearRole(){
        userRoles.clear();
    }

}
