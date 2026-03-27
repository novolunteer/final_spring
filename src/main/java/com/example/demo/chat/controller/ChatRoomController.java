package com.example.demo.chat.controller;

import com.example.demo.chat.dto.ChatRoomDetailResponse;
import com.example.demo.chat.dto.ChatRoomDto;
import com.example.demo.chat.dto.ChatRoomParticipantDto;
import com.example.demo.chat.dto.MessageSlice;
import com.example.demo.chat.service.ChatMessageService;
import com.example.demo.chat.service.ChatRoomService;
import com.example.demo.security.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatRoomController {
    private final ChatRoomService roomService;
    private final ChatMessageService messageService;

    @GetMapping("/chat/room/list")
    public ResponseEntity<?> chatRoomList(@AuthenticationPrincipal CustomUserDetails details){
        if (details == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","사용자 정보가 존재하지 않습니다."));
        }

        Integer userId=details.getUserId();
        if (userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","사용자 정보를 읽을 수 없습니다."));
        }

        try{
            List<ChatRoomDto> list=roomService.chatRoomList(userId);
            return ResponseEntity.ok(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","서버에 오류가 발생했습니다."));
        }
    }

    @GetMapping("/chat/room/{roomId}")
    public ResponseEntity<?> chatRoomDetail(@PathVariable Integer roomId,
                                                             @RequestParam(name = "cursor", required = false) Integer cursor,
                                                             @AuthenticationPrincipal CustomUserDetails details){
        if (details == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","사용자 정보가 존재하지 않습니다."));
        }

        Integer userId=details.getUserId();
        if (userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","사용자 정보를 읽을 수 없습니다."));
        }

        try{
            ChatRoomDto chatRoom=roomService.getChatRoom(roomId, userId);
            List<ChatRoomParticipantDto> participants=roomService.getParticipantInfo(roomId, userId);

            int size=20;
            MessageSlice messages=messageService.getMessages(roomId, cursor, size, userId);

            ChatRoomDetailResponse response=new ChatRoomDetailResponse(chatRoom, participants, messages);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","서버에 오류가 발생했습니다."));
        }
    }
}
