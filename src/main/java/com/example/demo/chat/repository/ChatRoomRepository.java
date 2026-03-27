package com.example.demo.chat.repository;

import com.example.demo.chat.entity.ChatRoom;
import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Integer> {
    List<ChatRoom> findByRoomIdInOrderByLastMessageAtDesc(List<Integer> roomIds);
    Optional<ChatRoom> findByRoomId(Integer roomId);
}
