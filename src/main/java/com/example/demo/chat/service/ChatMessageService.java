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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository messageRepository;
    private final ChatRoomRepository roomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;

    public ChatMessageDto sendUserMessage(SendMessageRequest message, Integer userId){
        ChatRoom room=roomRepository.findByRoomId(message.getRoomId()).orElseThrow(
                ()->new RuntimeException("채팅방이 존재하지 않습니다.")
        );

        User sender=userRepository.findByUserId(userId).orElseThrow(
                ()->new RuntimeException("존재하지 않는 사용자입니다.")
        );

        ChatRoomParticipant participantWhoSent=participantRepository
                .findByRoomAndUser_UserId(room, userId).orElseThrow(()->new RuntimeException(
                        "채팅방 또는 참가자가 존재하지 않습니다."
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

        return ChatMessageDto.builder()
                .messageId(saveMessage.getMessageId())
                .roomId(room.getRoomId())
                .senderId(sender.getUserId())
                .senderName(staff.getName())
                .messageType(saveMessage.getMessageType().name())
                .content(saveMessage.getContent())
                .createdAt(saveMessage.getCreatedAt()).build();
    }

    public MessageSlice getMessages(Integer roomId, Integer cursor, int size, Integer userId){
        Pageable pageable= PageRequest.of(0, size);
        Slice<ChatMessage> slice;

        if (cursor == null){
            slice=messageRepository.findByRoom_RoomIdOrderByMessageIdDesc(roomId, pageable);
        } else {
            slice=messageRepository.findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(roomId, cursor, pageable);
        }

        //발신자 이름 목록 얻기
        List<User> senders=slice.getContent().stream()
                .map(m -> m.getUser()).toList();
        List<Staff> staffs=staffRepository.findByUserIn(senders);
        Map<Integer, String> senderNames=staffs.stream().collect(Collectors.toMap(
                s -> s.getUser().getUserId(),
                s -> s.getName()
        ));

        List<ChatMessageDto> messages=slice.getContent().stream()
                .map(m -> ChatMessageDto.builder()
                        .messageId(m.getMessageId())
                        .roomId(m.getRoom().getRoomId())
                        .senderId(m.getUser().getUserId())
                        .senderName(senderNames.getOrDefault(m.getUser().getUserId(),null))
                        .content(m.getContent())
                        .messageType(m.getMessageType().name())
                        .createdAt(m.getCreatedAt())
                        .build()).toList();

        Integer nextCursor=null;
        if (!messages.isEmpty()){
            nextCursor=messages.get(messages.size() - 1).getMessageId();
        }

        return MessageSlice.builder()
                .messages(messages)
                .hasNext(slice.hasNext())
                .nextCursor(nextCursor).build();
    }
}
