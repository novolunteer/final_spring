package com.example.demo.chat.repository;

import com.example.demo.chat.entity.ChatAttachment;
import com.example.demo.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Integer> {
    List<ChatAttachment> findByMessage_MessageId(Integer messageId);
    List<ChatAttachment> findByMessageIn(List<ChatMessage> messages);
}
