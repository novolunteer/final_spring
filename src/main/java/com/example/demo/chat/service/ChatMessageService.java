package com.example.demo.chat.service;

import com.example.demo.chat.ChatMessageType;
import com.example.demo.chat.dto.ChatMessageDto;
import com.example.demo.chat.dto.MessageSlice;
import com.example.demo.chat.dto.SendMessageRequest;
import com.example.demo.chat.dto.UpdateMessageRequest;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final SimpMessagingTemplate messagingTemplate;

    public ChatMessageDto replyMessage(SendMessageRequest request, Integer userId){
        User user=userRepository.findByUserId(userId).orElseThrow(()->new RuntimeException("존재하지 않는 사용자입니다."));

        ChatRoom room=roomRepository.findByRoomId(request.getRoomId()).orElseThrow(()->new RuntimeException("채팅방이 존재하지 않습니다."));

        ChatMessage parentMessage=messageRepository.findByMessageId(request.getParentMessageId())
                .orElseThrow(()->new RuntimeException("존재하지 않는 메시지입니다."));

        if (parentMessage.getMessageType() == ChatMessageType.SYSTEM) {
            throw new RuntimeException("시스템 메시지에는 답장할 수 없습니다.");
        }

        if (!parentMessage.getRoom().getRoomId().equals(request.getRoomId())){
            throw new RuntimeException("같은 채팅방의 메시지에만 답장할 수 있습니다.");
        }

        ChatRoomParticipant participant=participantRepository.findByRoomAndUser_UserId(room, userId)
                .orElseThrow(()->new RuntimeException("채팅방의 참가자만 답장할 수 있습니다."));

        if (request.getContent() == null || request.getContent().trim().isEmpty()) {
            throw new RuntimeException("메시지 내용을 입력하세요.");
        }

        ChatMessage reply=messageRepository.save(ChatMessage.builder()
                .room(room)
                .user(user)
                .content(request.getContent().trim())
                .messageType(ChatMessageType.USER)
                .parentMessage(parentMessage)
                .build());

        room.setLastMessageId(reply.getMessageId());
        room.setLastMessageAt(reply.getCreatedAt());

        participant.setLastReadMessageId(reply.getMessageId());
        participant.setLastReadAt(reply.getCreatedAt());

        Staff staff=staffRepository.findByUser(user)
                .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));

        String parentUserName = null;

        if (parentMessage.getUser() != null) {
            Staff parent = staffRepository.findByUser(parentMessage.getUser())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 직원입니다."));
            parentUserName = parent.getName();
        }

        List<ChatRoomParticipant> participants=participantRepository.findByRoom(room);
        List<Integer> participantIds=participants.stream().map(p -> p.getUser().getUserId()).toList();

        Long unreadCount=participantRepository.countUnreadParticipants(room.getRoomId(), user.getUserId(),
                reply.getMessageId());

        return ChatMessageDto.builder()
                .messageId(reply.getMessageId())
                .roomId(room.getRoomId())
                .senderId(user.getUserId())
                .senderName(staff.getName())
                .messageType(reply.getMessageType().name())
                .content(reply.getContent())
                .createdAt(reply.getCreatedAt())
                .unreadCount(unreadCount)
                .mine(true)
                .deleted(reply.isDeleted())
                .deletedAt(reply.getDeletedAt())
                .edited(reply.isEdited())
                .editedAt(reply.getEditedAt())
                .parentMessageId(parentMessage.getMessageId())
                .parentMessageContent(parentMessage.isDeleted() ? null : parentMessage.getContent())
                .parentMessageIsDeleted(parentMessage.isDeleted())
                .parentMessageUserName(parentUserName)
                .participantIds(participantIds).build();
    }

    public ChatMessageDto deleteMessage(Integer messageId, Integer userId){
        ChatMessage message=messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 메시지입니다."));

        if(message.getMessageType() == ChatMessageType.SYSTEM){
            throw new IllegalArgumentException("시스템 메시지는 삭제할 수 없습니다.");
        }

        Staff staff=staffRepository.findByUser_UserId(userId)
                .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));

        if(!message.getUser().getUserId().equals(userId)){
            throw new IllegalArgumentException("본인 메시지만 삭제할 수 있습니다.");
        }

        if(message.isDeleted()){
            throw new IllegalArgumentException("이미 삭제된 메시지입니다.");
        }

        message.setDeleted(true);
        message.setDeletedAt(LocalDateTime.now());
        message.setContent(null);
        ChatMessage deleteMessage=messageRepository.save(message);

        List<ChatRoomParticipant> participants=participantRepository.findByRoom(deleteMessage.getRoom());
        List<Integer> participantIds=new ArrayList<>();
        for (ChatRoomParticipant p:participants){
            if (p.getUser().getUserId().equals(userId)) continue;
            participantIds.add(p.getUser().getUserId());
        }

        String parentMessageContent=null;
        String parentMessageUserName=null;
        boolean parentIsDeleted=false;

        ChatMessage parent=deleteMessage.getParentMessage();
        if (parent != null){
            parentMessageContent=parent.getContent();
            parentIsDeleted=parent.isDeleted();

            if (parent.getUser() != null){
                Staff parentStaff=staffRepository.findByUser(parent.getUser())
                        .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));
                parentMessageUserName=parentStaff.getName();
            }
        }

        return ChatMessageDto.builder()
                .messageId(deleteMessage.getMessageId())
                .roomId(deleteMessage.getRoom().getRoomId())
                .senderId(deleteMessage.getUser().getUserId())
                .senderName(staff.getName())
                .content(null)
                .mine(deleteMessage.getUser().getUserId().equals(userId))
                .messageType(deleteMessage.getMessageType().name())
                .createdAt(deleteMessage.getCreatedAt())
                .deleted(deleteMessage.isDeleted())
                .deletedAt(deleteMessage.getDeletedAt())
                .edited(deleteMessage.isEdited())
                .editedAt(deleteMessage.getEditedAt())
                .parentMessageId(parent != null ? parent.getMessageId() : null)
                .parentMessageContent(parentMessageContent)
                .parentMessageUserName(parentMessageUserName)
                .parentMessageIsDeleted(parentIsDeleted)
                .participantIds(participantIds)
                .lastMessage(deleteMessage.getMessageId().equals(deleteMessage.getRoom().getLastMessageId()))
                .build();
    }

    public ChatMessageDto editMessage(Integer messageId, String content, Integer userId){
        ChatMessage message=messageRepository.findByMessageId(messageId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 메시지입니다."));

        if(message.getMessageType() == ChatMessageType.SYSTEM){
            throw new IllegalArgumentException("시스템 메시지는 수정할 수 없습니다.");
        }

        Staff sendStaff=staffRepository.findByUser_UserId(userId)
                .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));

        if(!message.getUser().getUserId().equals(userId)){
            throw new IllegalArgumentException("본인 메시지만 수정할 수 있습니다.");
        }

        if(message.isDeleted()){
            throw new IllegalArgumentException("삭제된 메시지는 수정할 수 없습니다.");
        }

        if(content == null || content.trim().isEmpty()){
            throw new IllegalArgumentException("수정할 메시지 내용을 입력하세요.");
        }

        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
        message.setContent(content.trim());
        ChatMessage editMessage=messageRepository.save(message);

        List<ChatRoomParticipant> participants=participantRepository.findByRoom(editMessage.getRoom());
        List<Integer> participantIds=new ArrayList<>();
        for (ChatRoomParticipant p:participants){
            if (p.getUser().getUserId().equals(userId)) continue;
            participantIds.add(p.getUser().getUserId());
        }

        String parentMessageContent=null;
        String parentMessageUserName=null;
        boolean parentIsDeleted=false;

        ChatMessage parent=editMessage.getParentMessage();
        if (parent != null){
            parentMessageContent=parent.getContent();
            parentIsDeleted=parent.isDeleted();

            if (parent.getUser() != null){
                Staff staff=staffRepository.findByUser(parent.getUser())
                        .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));
                parentMessageUserName=staff.getName();
            }
        }

        return ChatMessageDto.builder()
                .messageId(editMessage.getMessageId())
                .roomId(editMessage.getRoom().getRoomId())
                .senderName(sendStaff.getName())
                .senderId(editMessage.getUser().getUserId())
                .content(editMessage.getContent())
                .mine(editMessage.getUser().getUserId().equals(userId))
                .messageType(editMessage.getMessageType().name())
                .createdAt(editMessage.getCreatedAt())
                .edited(editMessage.isEdited())
                .editedAt(editMessage.getEditedAt())
                .deleted(editMessage.isDeleted())
                .deletedAt(editMessage.getDeletedAt())
                .parentMessageId(parent != null ? parent.getMessageId() : null)
                .parentMessageContent(parentMessageContent)
                .parentMessageUserName(parentMessageUserName)
                .parentMessageIsDeleted(parentIsDeleted)
                .participantIds(participantIds)
                .lastMessage(editMessage.getMessageId().equals(editMessage.getRoom().getLastMessageId()))
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
        List<Integer> participantIds=participants.stream().map(p -> p.getUser().getUserId()).toList();

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
                .participantIds(participantIds).build();
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

        ChatMessage parent=message.getParentMessage();
        if (parent != null){
            parentMessageContent=parent.getContent();
            parentIsDeleted=parent.isDeleted();

            if (parent.getUser() != null){
                Staff staff=staffRepository.findByUser(parent.getUser())
                        .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));
                parentMessageUserName=staff.getName();
            }
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
                .deleted(message.isDeleted())
                .deletedAt(message.getDeletedAt())
                .edited(message.isEdited())
                .parentMessageId(message.getParentMessage() != null ? message.getParentMessage().getMessageId() : null)
                .parentMessageContent(parentMessageContent != null ? parentMessageContent : null)
                .parentMessageUserName(parentMessageUserName != null ? parentMessageUserName : null)
                .parentMessageIsDeleted(parentIsDeleted)
                .build();
    }
}
