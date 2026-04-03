package com.example.demo.chat.controller;

import com.example.demo.chat.dto.ChatAttachmentDto;
import com.example.demo.chat.service.ChatAttachmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChatAttachmentController {
    private final ChatAttachmentService attachmentService;

    @PostMapping("/chat/upload/attachment")
    public ResponseEntity<Map<String,Object>> uploadAttachment(@RequestParam("files") List<MultipartFile> files){
        List<ChatAttachmentDto> result=attachmentService.uploadFiles(files);
        return ResponseEntity.ok(Map.of("result", result));
    }
}
