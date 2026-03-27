package com.example.demo.chat.repository;

import com.example.demo.chat.entity.ChatMessage;
import com.example.demo.chat.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
    Optional<ChatMessage> findByMessageId(Integer messageId);
    List<ChatMessage> findByMessageIdIn(List<Integer> messageIds);
    Slice<ChatMessage> findByRoom_RoomIdOrderByMessageIdDesc(Integer roomId, Pageable pageable);
    Slice<ChatMessage> findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(Integer roomId, Integer cursor, Pageable pageable);
}
