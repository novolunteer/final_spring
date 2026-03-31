package com.example.demo.chat.service;

import com.example.demo.chat.ChatMessageType;
import com.example.demo.chat.dto.ChatMessageDto;
import com.example.demo.chat.dto.MessageSlice;
import com.example.demo.chat.dto.SendMessageRequest;
import com.example.demo.chat.entity.ChatMessage;
import com.example.demo.chat.entity.ChatRoom;
import com.example.demo.chat.entity.ChatRoomParticipant;
import com.example.demo.chat.repository.ChatMessageRepository;
import com.example.demo.chat.repository.ChatRoomParticipantRepository;
import com.example.demo.chat.repository.ChatRoomRepository;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository roomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final ChatRoomService roomService;

    public ChatMessageDto sendUserMessage(SendMessageRequest message, Integer userId){
        ChatRoom room=roomRepository.findByRoomId(message.getRoomId()).orElseThrow(
                ()->new RuntimeException("채팅방이 존재하지 않습니다.")
        );

        User sender=userRepository.findByUserId(userId).orElseThrow(
                ()->new RuntimeException("존재하지 않는 사용자입니다.")
        );

        ChatRoomParticipant participantWhoSent=participantRepository
                .findByRoomAndUser_UserId(room, userId).orElseThrow(()->new RuntimeException(
                        "채팅방 참가자가 아닙니다."
                ));

        if (message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new RuntimeException("메시지 내용을 입력하세요.");
        }

        ChatMessage saveMessage=messageRepository.save(ChatMessage.builder()
                .room(room).user(sender)
                .messageType(ChatMessageType.USER)
                .content(message.getContent()).build());

        room.setLastMessageId(saveMessage.getMessageId());
        room.setLastMessageAt(saveMessage.getCreatedAt());

        participantWhoSent.setLastReadMessageId(saveMessage.getMessageId());
        participantWhoSent.setLastReadAt(saveMessage.getCreatedAt());

        Staff staff=staffRepository.findByUser(sender)
                .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));

        List<ChatRoomParticipant> participants=participantRepository.findByRoom_RoomId(message.getRoomId());

        Long unreadCount=participantRepository.countUnreadParticipants(room.getRoomId(), sender.getUserId(),
                saveMessage.getMessageId());

        return ChatMessageDto.builder()
                .messageId(saveMessage.getMessageId())
                .roomId(room.getRoomId())
                .senderId(sender.getUserId())
                .senderName(staff.getName())
                .messageType(saveMessage.getMessageType().name())
                .content(saveMessage.getContent())
                .createdAt(saveMessage.getCreatedAt())
                .unreadCount(unreadCount)
                .mine(true)
                .participantIds(participants.stream().map(p -> p.getUser().getUserId()).toList()).build();
    }

    public MessageSlice getMessages(Integer roomId, Integer cursor, int size, Integer userId){
        Pageable pageable= PageRequest.of(0, size);
        Slice<ChatMessage> slice;

        if (cursor == null){
            slice=messageRepository.findByRoom_RoomIdOrderByMessageIdDesc(roomId, pageable);
        } else {
            slice=messageRepository.findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(roomId, cursor, pageable);
        }

        List<ChatMessageDto> messages=slice.getContent().stream()
                .map(message -> toDto(message, userId)).toList();

        Integer nextCursor=null;
        if (!messages.isEmpty()){
            nextCursor=messages.get(messages.size() - 1).getMessageId();
        }

        MessageSlice messageSlice=MessageSlice.builder()
                .messages(messages)
                .hasNext(slice.hasNext())
                .nextCursor(nextCursor).build();


        if (cursor == null){
            roomService.markAsRead(roomId, userId);
        }

        return messageSlice;
    }

    private ChatMessageDto toDto(ChatMessage message, Integer loginUserId){
        Long unreadCount=0L;

        if (message.getMessageType() == ChatMessageType.USER && message.getUser() != null){
            unreadCount=participantRepository.countUnreadParticipants(
                    message.getRoom().getRoomId(), message.getUser().getUserId(),
                    message.getMessageId()
            );
        }

        Staff staff=staffRepository.findByUser_UserId(message.getUser().getUserId())
                .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다. (메시지 발신자)"));

        return ChatMessageDto.builder()
                .messageId(message.getMessageId())
                .roomId(message.getRoom().getRoomId())
                .senderId(message.getUser() != null ? message.getUser().getUserId() : null)
                .senderName(staff.getName())
                .messageType(message.getMessageType().name())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .unreadCount(unreadCount)
                .mine(
                        message.getUser() != null && message.getUser().getUserId().equals(loginUserId)
                ).build();
    }
}
