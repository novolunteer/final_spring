package com.example.demo.chat.controller;

import com.example.demo.chat.dto.ChatAttachmentDto;
import com.example.demo.chat.service.ChatAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatAttachmentController {
    private final ChatAttachmentService attachmentService;

    @PostMapping("/api/chat/attachment/upload")
    public ResponseEntity<Map<String,Object>> uploadAttachment(@RequestParam("file")MultipartFile file){
        ChatAttachmentDto result=attachmentService.uploadFile(file);
        return ResponseEntity.ok(Map.of("result", result));
    }
}
