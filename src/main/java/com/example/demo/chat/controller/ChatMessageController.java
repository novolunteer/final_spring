package com.example.demo.chat.controller;

import com.example.demo.chat.dto.ChatMessageDto;
import com.example.demo.chat.dto.SendMessageRequest;
import com.example.demo.chat.service.ChatMessageService;
import com.example.demo.security.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService messageService;

    @PostMapping("/chat/send/user")
    public ResponseEntity<?> sendUserMessage(@RequestBody SendMessageRequest dto,
                                             @AuthenticationPrincipal CustomUserDetails details){
        if (details == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error","로그인 후 이용하세요."));
        }

        Integer userId=details.getUserId();
        if (userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "사용자 정보를 찾을 수 없습니다."));
        }

        try{
            ChatMessageDto message=messageService.sendUserMessage(dto, userId);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error","서버 오류!"));
        }
    }
}
