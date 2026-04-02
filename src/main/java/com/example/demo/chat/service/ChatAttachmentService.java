package com.example.demo.chat.service;

import com.example.demo.chat.dto.ChatAttachmentDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class ChatAttachmentService {
    @Value("${file.upload-dir}")
    private String uploadDir;

    public ChatAttachmentDto uploadFile(MultipartFile file){
        if (file == null || file.isEmpty()){
            throw new RuntimeException("업로드 할 파일이 없습니다.");
        }

        String originalFileName= file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()){
            throw new RuntimeException("파일명이 올바르지 않습니다.");
        }

        String fileExtension=getFileExtension(originalFileName);

        String storedFileName= UUID.randomUUID().toString();
        if (!fileExtension.isBlank()){
            storedFileName += "." + fileExtension;
        }

        String contentType=file.getContentType();
        if (contentType == null || contentType.isBlank()){
            contentType = "application/octet-stream";
        }

        Long fileSize=file.getSize();

        Path chatUploadPath= Paths.get(uploadDir, "chat");

        try{
            if (Files.notExists(chatUploadPath)){
                Files.createDirectories(chatUploadPath);
            }

            Path savePath=chatUploadPath.resolve(storedFileName);
            file.transferTo(savePath.toFile());
        } catch (Exception e) {
            throw new RuntimeException("파일 저장 중 오류가 발생했습니다.");
        }

        String fileUrl="/upload/chat/" + storedFileName;

        return ChatAttachmentDto.builder()
                .originalFileName(originalFileName)
                .storedFileName(storedFileName)
                .fileUrl(fileUrl)
                .contentType(contentType)
                .fileExtension(fileExtension)
                .fileSize(fileSize)
                .thumbnailUrl(null)
                .build();
    }

    private String getFileExtension(String fileName){
        int lastDotIndex=fileName.lastIndexOf(".");
        if (lastDotIndex == 1 || lastDotIndex == fileName.length() - 1){
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
