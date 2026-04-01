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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
    private final ChatRoomService roomService;

    public ChatMessageDto deleteMessage(Integer messageId, Integer userId){
        ChatMessage message=messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 메시지입니다."));

        if(!message.getUser().getUserId().equals(userId)){
            throw new IllegalArgumentException("본인 메시지만 삭제할 수 있습니다.");
        }

        if(message.isDeleted()){
            throw new IllegalArgumentException("이미 삭제된 메시지입니다.");
        }

        if(message.getMessageType() == ChatMessageType.SYSTEM){
            throw new IllegalArgumentException("시스템 메시지는 삭제할 수 없습니다.");
        }

        message.setDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setContent("삭제된 메시지입니다.");
        ChatMessage deleteMessage=messageRepository.save(message);

        String parentMessageContent=null;
        String parentMessageUserName=null;
        boolean parentIsDeleted=false;

        if (deleteMessage.getParentMessage().getMessageId() != null){
            ChatMessage parent=messageRepository.findByMessageId(deleteMessage.getParentMessage().getMessageId())
                    .orElseThrow(()->new RuntimeException("부모 메시지가 존재하지 않습니다."));
            parentMessageContent=parent.getContent();
            Staff staff=staffRepository.findByUser(parent.getUser())
                    .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));
            parentMessageUserName=staff.getName();
            parentIsDeleted=parent.isDeleted();
        }

        return ChatMessageDto.builder()
                .messageId(deleteMessage.getMessageId())
                .content("삭제된 메시지입니다.")
                .mine(deleteMessage.getUser().getUserId().equals(userId))
                .messageType(deleteMessage.getMessageType().name())
                .isDeleted(deleteMessage.isDeleted())
                .deletedAt(deleteMessage.getDeletedAt())
                .isEdited(deleteMessage.isEdited())
                .editedAt(deleteMessage.getEditedAt())
                .parentMessageId(deleteMessage.getParentMessage() != null ? deleteMessage.getParentMessage().getMessageId() : null)
                .parentMessageContent(parentMessageContent != null ? parentMessageContent : null)
                .parentMessageUserName(parentMessageUserName != null ? parentMessageUserName : null)
                .isParentMessageDeleted(parentIsDeleted)
                .build();
    }

    public ChatMessageDto editMessage(Integer messageId, String content, Integer userId){
        ChatMessage message=messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 메시지입니다."));

        if(!message.getUser().getUserId().equals(userId)){
            throw new IllegalArgumentException("본인 메시지만 수정할 수 있습니다.");
        }

        if(message.isDeleted()){
            throw new IllegalArgumentException("삭제된 메시지는 수정할 수 없습니다.");
        }

        if(message.getMessageType() == ChatMessageType.SYSTEM){
            throw new IllegalArgumentException("시스템 메시지는 수정할 수 없습니다.");
        }

        if(content == null || content.trim().isEmpty()){
            throw new IllegalArgumentException("수정할 메시지 내용을 입력하세요.");
        }

        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
        message.setContent(content);
        ChatMessage editMessage=messageRepository.save(message);

        String parentMessageContent=null;
        String parentMessageUserName=null;
        boolean parentIsDeleted=false;

        if (editMessage.getParentMessage().getMessageId() != null){
            ChatMessage parent=messageRepository.findByMessageId(editMessage.getParentMessage().getMessageId())
                    .orElseThrow(()->new RuntimeException("부모 메시지가 존재하지 않습니다."));
            parentMessageContent=parent.getContent();
            Staff staff=staffRepository.findByUser(parent.getUser())
                    .orElseThrow(()-> new RuntimeException("존재하지 않는 직원입니다."));
            parentMessageUserName=staff.getName();
            parentIsDeleted=parent.isDeleted();
        }

        return ChatMessageDto.builder()
                .messageId(editMessage.getMessageId())
                .content(editMessage.getContent())
                .mine(editMessage.getUser().getUserId().equals(userId))
                .messageType(editMessage.getMessageType().name())
                .isEdited(editMessage.isEdited())
                .editedAt(editMessage.getEditedAt())
                .isDeleted(editMessage.isDeleted())
                .deletedAt(editMessage.getDeletedAt())
                .parentMessageId(editMessage.getParentMessage() != null ? editMessage.getParentMessage().getMessageId() : null)
                .parentMessageContent(parentMessageContent != null ? parentMessageContent:null)
                .parentMessageUserName(parentMessageUserName != null ? parentMessageUserName : null)
                .isParentMessageDeleted(parentIsDeleted)
                .build();
    }

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

        ChatRoomParticipant participant=participantRepository.findByRoom_RoomIdAndUser_UserId(roomId, userId)
                .orElseThrow(()->new RuntimeException("현재 참여 중인 채팅방이 아닙니다."));

        LocalDateTime joinedAt=participant.getJoinedAt();

        if (cursor == null){
            slice=messageRepository.findByRoom_RoomIdAndCreatedAtGreaterThanEqualOrderByMessageIdDesc(
                    roomId, joinedAt, pageable
            );
        } else {
            slice=messageRepository.findByRoom_RoomIdAndCreatedAtGreaterThanEqualAndMessageIdLessThanOrderByMessageIdDesc(
                    roomId, joinedAt, cursor, pageable
            );
        }

        List<ChatMessageDto> messages=slice.getContent().stream()
                .map(message -> toDto(message, userId)).collect(Collectors.toList());

        Collections.reverse(messages);

        Integer nextCursor=null;
        if (!messages.isEmpty()){
            nextCursor=messages.get(0).getMessageId();
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
        User sender = message.getUser();
        Long unreadCount = 0L;
        String senderName = null;

        if (message.getMessageType() == ChatMessageType.USER && sender != null){
            unreadCount = participantRepository.countUnreadParticipants(
                    message.getRoom().getRoomId(),
                    sender.getUserId(),
                    message.getMessageId()
            );
        }

        if (sender != null){
            Staff staff = staffRepository.findByUser_UserId(sender.getUserId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 직원입니다. (메시지 발신자)"));
            senderName = staff.getName();
        }

        String parentMessageContent=null;
        String parentMessageUserName=null;
        boolean parentIsDeleted=false;

        if (message.getParentMessage().getMessageId() != null){
            ChatMessage parent=messageRepository.findByMessageId(message.getParentMessage().getMessageId())
                    .orElseThrow(()->new RuntimeException("부모 메시지가 존재하지 않습니다."));
            parentMessageContent=parent.getContent();
            Staff staff=staffRepository.findByUser(parent.getUser())
                    .orElseThrow(()-> new RuntimeException("존재하지 않는 직원입니다."));
            parentMessageUserName=staff.getName();
            parentIsDeleted=parent.isDeleted();
        }

        return ChatMessageDto.builder()
                .messageId(message.getMessageId())
                .roomId(message.getRoom().getRoomId())
                .senderId(sender != null ? sender.getUserId() : null)
                .senderName(senderName)
                .messageType(message.getMessageType().name())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .unreadCount(unreadCount)
                .mine(sender != null && sender.getUserId().equals(loginUserId))
                .isDeleted(message.isDeleted())
                .deletedAt(message.getDeletedAt())
                .isEdited(message.isEdited())
                .parentMessageId(message.getParentMessage() != null ? message.getParentMessage().getMessageId() : null)
                .parentMessageContent(parentMessageContent != null ? parentMessageContent : null)
                .parentMessageUserName(parentMessageUserName != null ? parentMessageUserName : null)
                .isParentMessageDeleted(parentIsDeleted)
                .build();
    }
}
