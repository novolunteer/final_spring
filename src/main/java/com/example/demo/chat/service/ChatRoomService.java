package com.example.demo.chat.service;

import com.example.demo.chat.dto.ChatRoomDto;
import com.example.demo.chat.dto.ChatRoomParticipantDto;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {
    private final ChatRoomRepository roomRepository;
    private final ChatRoomParticipantRepository participantRepository;
    private final UserRepository userRepository;
    private final ChatMessageRepository messageRepository;
    private final StaffRepository staffRepository;

    public List<ChatRoomParticipantDto> getParticipantInfo(Integer roomId, Integer userId){
        List<ChatRoomParticipant> participants=participantRepository.findByRoom_RoomId(roomId);

        //참가자 이름 구하기
        List<User> users=participants.stream().map(p -> p.getUser()).toList();
        List<Staff> staff=staffRepository.findByUserIn(users);
        Map<Integer, String> names=staff.stream().collect(Collectors.toMap(
                s -> s.getUser().getUserId(),
                s -> s.getName()
        ));

        List<ChatRoomParticipantDto> list=participants.stream()
                .map(p -> ChatRoomParticipantDto.builder()
                        .participantId(p.getParticipantId())
                        .roomId(p.getRoom().getRoomId())
                        .userId(p.getUser().getUserId())
                        .userName(names.getOrDefault(p.getUser().getUserId(), null))
                        .customRoomName(p.getCustomRoomName())
                        .me(p.getUser().getUserId().equals(userId))
                        .joinedAt(p.getJoinedAt())
                        .lastReadMessageId(p.getLastReadMessageId())
                        .lastReadAt(p.getLastReadAt()).build()).toList();
        return list;
    }

    public ChatRoomDto getChatRoom(Integer roomId, Integer userId){
        ChatRoom chatRoom=roomRepository.findByRoomId(roomId).orElseThrow(()->new RuntimeException("존재하지 않는 채팅방입니다."));

        ChatMessage message=messageRepository.findByMessageId(chatRoom.getLastMessageId()).orElseThrow(()
        ->new RuntimeException("존재하지 않는 메시지입니다."));

        List<ChatRoomParticipant> participants=participantRepository.findByRoom(chatRoom);
        ChatRoomParticipant userCustomRoom=participantRepository.findByRoomAndUser_UserId(chatRoom, userId)
                .orElseThrow(()-> new RuntimeException("채팅방 혹은 유저 정보가 존재하지 않습니다."));

        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomType(chatRoom.getRoomType().name())
                .roomName(chatRoom.getRoomName())
                .createdBy(chatRoom.getUser().getUserId())
                .lastMessageId(chatRoom.getLastMessageId())
                .lastMessageAt(chatRoom.getLastMessageAt())
                .lastMessageText(message.getContent())
                .participantCount(Long.valueOf(participants.size()))
                .customRoomName(userCustomRoom.getCustomRoomName()).build();
    }

    public List<ChatRoomDto> chatRoomList(Integer userId){
        User user=userRepository.findByUserId(userId).orElseThrow(()->new RuntimeException("사용자를 찾을 수 없습니다."));
        List<ChatRoomParticipant> participants=participantRepository.findByUser(user);
        if (participants.isEmpty()){
            return List.of();
        }

        Map<Integer, ChatRoomParticipant> myParticipantMap=participants.stream()
                .collect(Collectors.toMap(
                        p -> p.getRoom().getRoomId(),
                        p -> p
                ));

        List<ChatRoom> chatRooms = roomRepository.findByRoomIdInOrderByLastMessageAtDesc(
                participants.stream().map(p -> p.getRoom().getRoomId()).toList()
        );

        List<ChatRoomParticipant> countList=participantRepository.findByRoomIn(chatRooms);

        Map<Integer, Long> counts=countList.stream().collect(Collectors.groupingBy(
                p -> p.getRoom().getRoomId(),
                Collectors.counting()
        ));

        Map<Integer, String> customRoomNames=participants.stream().filter(p -> p.getCustomRoomName() != null)
                .collect(Collectors.toMap(
                        p -> p.getRoom().getRoomId(),
                        p -> p.getCustomRoomName()
                ));

        List<Integer> messageIds=chatRooms.stream().map(m -> m.getLastMessageId())
                .filter(id -> id != null).toList();
        List<ChatMessage> messages=messageRepository.findByMessageIdIn(messageIds);
        Map<Integer, String> lastMessages=messages.stream().filter(m -> m.getContent() != null)
                .collect(Collectors.toMap(
           m -> m.getMessageId(),
           m -> m.getContent()
        ));

        List<ChatRoomDto> rooms=chatRooms.stream().map(c -> {
            ChatRoomParticipant participant=myParticipantMap.get(c.getRoomId());

            Integer lastReadMessageId=participant != null ? participant.getLastReadMessageId() : null;

            int unreadCount;

            if (lastReadMessageId == null){
                unreadCount=messageRepository.countByRoom_RoomId(c.getRoomId());
            } else {
                unreadCount=messageRepository.countByRoom_RoomIdAndMessageIdGreaterThan(
                        c.getRoomId(), lastReadMessageId
                );
            }

            return ChatRoomDto.builder()
                    .roomId(c.getRoomId())
                    .roomType(c.getRoomType().name())
                    .roomName(c.getRoomName())
                    .createdBy(c.getUser().getUserId())
                    .lastMessageId(c.getLastMessageId())
                    .lastMessageText(lastMessages.getOrDefault(c.getLastMessageId(), null))
                    .lastMessageAt(c.getLastMessageAt())
                    .customRoomName(customRoomNames.getOrDefault(c.getRoomId(), null))
                    .participantCount(counts.getOrDefault(c.getRoomId(), 0L))
                    .unreadCount(unreadCount).build();
        }).toList();

        return rooms;
    }
}

