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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    private ChatMessage lastMessageId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_at")
    private ChatMessage lastMessageAt;

    private LocalDateTime createdAt;
}
