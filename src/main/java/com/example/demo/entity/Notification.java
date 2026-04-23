package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long notificationId;
    private Long userId = 1L;
    private String writer = "관리자";
    private String title;
    private String content;
    private int viewCount = 0;
    private boolean important;  //  주요공지

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    }
