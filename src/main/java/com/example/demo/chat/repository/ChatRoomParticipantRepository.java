package com.example.demo.chat.repository;

import com.example.demo.chat.entity.ChatRoom;
import com.example.demo.chat.entity.ChatRoomParticipant;
import com.example.demo.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Integer> {
    List<ChatRoomParticipant> findByUser(User user);
    List<ChatRoomParticipant> findByRoomIn(List<ChatRoom> chatRooms);
    List<ChatRoomParticipant> findByRoom(ChatRoom room);
    Optional<ChatRoomParticipant> findByRoomAndUser_UserId(ChatRoom room, Integer userId);
    List<ChatRoomParticipant> findByRoom_RoomId(Integer roomId);
    Optional<ChatRoomParticipant> findByRoom_RoomIdAndUser_UserId(Integer roomId, Integer userId);
}
