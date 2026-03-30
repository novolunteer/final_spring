package com.example.demo.chat.service;

import com.example.demo.chat.ChatRoomType;
import com.example.demo.chat.dto.ChatRoomDto;
import com.example.demo.chat.dto.ChatRoomParticipantDto;
import com.example.demo.chat.dto.CreateChatRoomRequest;
import com.example.demo.chat.dto.GetStaffListResponse;
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
import com.example.demo.userRole.UserRole;
import com.example.demo.userRole.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final UserRoleRepository userRoleRepository;
    private final SimpMessagingTemplate messagingTemplate;


    public List<GetStaffListResponse> getStaffList(Integer userId, String keyword){
        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        }

        List<Staff> staffs=staffRepository.searchStaff(userId, keyword);

        //직원 역할 id 목록
        List<UserRole> userRoles=userRoleRepository.findTopRoleByUsers(staffs.stream().map(
                u -> u.getUser()
        ).toList());

        Map<Integer, String> roles=userRoles.stream().collect(Collectors.toMap(
                r -> r.getUser().getUserId(),
                r -> r.getRole().getRoleName()
        ));

        List<GetStaffListResponse> responses=staffs.stream().map(u -> GetStaffListResponse.builder()
                .userId(u.getUser().getUserId())
                .username(u.getName())
                .department(u.getDepartment().getDepartmentName())
                .role(roles.getOrDefault(u.getUser().getUserId(), null)).build()).toList();

        return responses;
    }

    public ChatRoomDto createChatRoom(Integer userId, CreateChatRoomRequest request){
        User user=userRepository.findByUserId(userId).orElseThrow(()->new RuntimeException("존재하지 않는 사용자입니다."));

        String roomName=null;
        String roomType=request.getRoomType();
        if (!"GROUP".equals(roomType) && !"DIRECT".equals(roomType)) {
            throw new RuntimeException("채팅방 타입이 올바르지 않습니다.");
        }

        List<Integer> participants=request.getParticipantUserIds();
        if (participants == null || participants.isEmpty()) {
            throw new RuntimeException("채팅방 참여자를 선택하세요.");
        }
        List<Integer> participantIds = participants.stream()
                .filter(id -> !id.equals(userId))
                .distinct()
                .toList();

        if ("DIRECT".equals(roomType) && participantIds.size() != 1) {
            throw new RuntimeException("1:1 채팅은 한 명만 선택할 수 있습니다.");
        }

        if ("GROUP".equals(roomType) && participantIds.size() < 2) {
            throw new RuntimeException("그룹 채팅 참여자를 두 명 이상 선택하세요.");
        }

        List<String> members=new ArrayList<>();
        ChatRoomType type;

        if ("GROUP".equals(roomType)){
            for (int i=0;i<participantIds.size();i++){
                Staff staff=staffRepository.findByUser_UserId(participantIds.get(i))
                        .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));
                members.add(staff.getName());
            }

            if (request.getRoomName() != null && !request.getRoomName().isBlank()) {
                roomName = request.getRoomName();
            } else {
                roomName = String.join("&", members);
            }
            type=ChatRoomType.GROUP;
        } else {
            ChatRoom existingRoom=roomRepository.findExistingDirectRoom(ChatRoomType.DIRECT, userId, participantIds.get(0))
                    .orElse(null);
            if (existingRoom != null){
                ChatRoomParticipant me = participantRepository.findByRoomAndUser_UserId(existingRoom, userId)
                        .orElseThrow(() -> new RuntimeException("채팅방 참가자가 존재하지 않습니다."));

                return ChatRoomDto.builder()
                        .roomId(existingRoom.getRoomId())
                        .roomType(existingRoom.getRoomType().name())
                        .roomName(existingRoom.getRoomName())
                        .createdBy(existingRoom.getUser().getUserId())
                        .customRoomName(me.getCustomRoomName())
                        .participantCount(2L).build();
            }

            Staff me=staffRepository.findByUser_UserId(userId)
                    .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다. (채팅방 생성자)"));
            Staff you=staffRepository.findByUser_UserId(participantIds.get(0))
                    .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));
            roomName=me.getName() + "&" + you.getName();
            type=ChatRoomType.DIRECT;
        }

        ChatRoom room=roomRepository.save(ChatRoom.builder()
                .roomType(type)
                .roomName(roomName)
                .user(user).build());

        ChatRoomParticipant master=participantRepository.save(
                ChatRoomParticipant.builder()
                        .room(room)
                        .user(user)
                        .customRoomName(request.getCustomRoomName() != null && !request.getCustomRoomName().isBlank()
                                ? request.getCustomRoomName() : null).build()
        );

        for (int i=0; i<participantIds.size();i++){
            User participant=userRepository.findByUserId(participantIds.get(i))
                    .orElseThrow(()-> new RuntimeException("존재하지 않는 사용자입니다. (채팅방 참여자)"));
            participantRepository.save(ChatRoomParticipant.builder()
                    .room(room)
                    .user(participant).build());
        }

        ChatRoomDto roomForParticipant=makeNewRoomDto(room);
        for (int i=0;i<participantIds.size();i++){
            ChatRoomParticipant participant=participantRepository.findByRoomAndUser_UserId(room, participantIds.get(i))
                    .orElseThrow(()->new RuntimeException("존재하지 않는 사용자입니다. (채팅방 참여자)"));
            messagingTemplate.convertAndSend(
                    "/topic/chat/room-created/" + participant.getUser().getUserId(), roomForParticipant
            );
        }

        Long participantCount= 1L + participantIds.size();

        return ChatRoomDto.builder()
                .roomId(room.getRoomId())
                .roomType(room.getRoomType().name())
                .roomName(room.getRoomName())
                .createdBy(room.getUser().getUserId())
                .customRoomName(master.getCustomRoomName())
                .participantCount(participantCount).build();
    }

    public ChatRoomDto makeNewRoomDto(ChatRoom room){
        Integer participantCount=participantRepository.countByRoom(room);
        return ChatRoomDto.builder()
                .roomId(room.getRoomId())
                .roomType(room.getRoomType().name())
                .roomName(room.getRoomName())
                .createdBy(room.getUser().getUserId())
                .participantCount(Long.valueOf(participantCount)).build();
    }

    public void markAsRead(Integer roomId, Integer userId){
        ChatRoomParticipant participant=participantRepository
                .findByRoom_RoomIdAndUser_UserId(roomId, userId)
                .orElseThrow(()->new RuntimeException("채팅방 참여자 정보가 없습니다."));

        ChatRoom room=roomRepository.findByRoomId(roomId)
                .orElseThrow(()->new RuntimeException("채팅방이 존재하지 않습니다."));

        participant.setLastReadMessageId(room.getLastMessageId());
        participant.setLastReadAt(LocalDateTime.now());
    }

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

        List<ChatRoomParticipant> participants=participantRepository.findByRoom(chatRoom);
        ChatRoomParticipant userCustomRoom=participantRepository.findByRoomAndUser_UserId(chatRoom, userId)
                .orElseThrow(()-> new RuntimeException("채팅방 혹은 유저 정보가 존재하지 않습니다."));

        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomType(chatRoom.getRoomType().name())
                .roomName(chatRoom.getRoomName())
                .createdBy(chatRoom.getUser().getUserId())
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

