package com.example.demo.chat.repository;

import com.example.demo.chat.entity.ChatAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatAttachmentRepository extends JpaRepository<ChatAttachment, Integer> {
}
