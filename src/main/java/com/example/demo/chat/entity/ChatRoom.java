package com.example.demo.chat.entity;

import com.example.demo.chat.ChatRoomType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer roomId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatRoomType chatRoomType;

    private String roomName;

    @Column(nullable = false)
    private Integer createdBy;

    private LocalDateTime createdAt;
}
