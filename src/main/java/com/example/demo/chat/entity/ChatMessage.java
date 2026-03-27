package com.example.demo.chat.entity;

import com.example.demo.chat.ChatMessageType;
import com.example.demo.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")
    private User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ChatMessageType messageType;

    private String content;

    @CreationTimestamp
    private LocalDateTime createdAt;

}
