package com.example.demo.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class ChatMessageDto {
    private Integer messageId;
    private Integer roomId;
    private Integer senderId;
    private String senderName;
    private String messageType;
    private String content;
    private LocalDateTime createdAt;
    private Integer unreadCount;
    private boolean mine;
    private List<ChatAttachmentDto> attachments;
    private List<Integer> participantIds;
}

