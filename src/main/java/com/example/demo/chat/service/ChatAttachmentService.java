package com.example.demo.chat.service;

import com.example.demo.chat.dto.ChatAttachmentDto;
import com.example.demo.chat.dto.ChatAttachmentSlice;
import com.example.demo.chat.dto.ChatAttachmentSummaryDto;
import com.example.demo.chat.entity.ChatAttachment;
import com.example.demo.chat.entity.ChatRoom;
import com.example.demo.chat.entity.ChatRoomParticipant;
import com.example.demo.chat.repository.ChatAttachmentRepository;
import com.example.demo.chat.repository.ChatRoomParticipantRepository;
import com.example.demo.chat.repository.ChatRoomRepository;
import com.example.demo.staff.Staff;
import com.example.demo.staff.StaffRepository;
import com.example.demo.user.User;
import com.example.demo.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatAttachmentService {
    @Value("${file.upload-dir}")
    private String uploadDir;
    private final UserRepository userRepository;
    private final StaffRepository staffRepository;
    private final ChatRoomRepository roomRepository;
    private final ChatAttachmentRepository attachmentRepository;
    private final ChatRoomParticipantRepository participantRepository;

    public ChatAttachmentSlice getChatRoomAttachments(Integer userId, Integer roomId, Integer cursor, int size){
        User user=userRepository.findByUserId(userId)
                .orElseThrow(()->new RuntimeException("존재하지 않는 사용자입니다."));

        ChatRoom room=roomRepository.findByRoomId(roomId)
                .orElseThrow(()->new RuntimeException("채팅방이 존재하지 않습니다."));

        ChatRoomParticipant participant=participantRepository.findByRoomAndUser(room, user)
                .orElseThrow(()->new RuntimeException("채팅방 참가자가 아닙니다."));

        LocalDateTime joinedAt=participant.getJoinedAt();

        Pageable pageable= PageRequest.of(0, size);
        Slice<ChatAttachment> slice;

        if (cursor == null){
            slice=attachmentRepository.findByRoomId(room.getRoomId(), joinedAt, pageable);
        } else {
            slice= attachmentRepository.findByRoomIdAndMessageIdLessThan(room.getRoomId(), joinedAt, cursor, pageable);
        }

        List<ChatAttachmentSummaryDto> attachmentSummaries=slice.getContent().stream().map(a -> {
            Staff staff=staffRepository.findByUser(a.getMessage().getUser())
                    .orElseThrow(()->new RuntimeException("존재하지 않는 직원입니다."));

                    return ChatAttachmentSummaryDto.builder()
                            .roomId(room.getRoomId())
                            .userId(staff.getUser().getUserId())
                            .username(staff.getName())
                            .messageId(a.getMessage().getMessageId())
                            .attachmentId(a.getAttachmentId())
                            .originalFileName(a.getOriginalFileName())
                            .storedFileName(a.getStoredFileName())
                            .fileUrl(a.getFileUrl())
                            .contentType(a.getContentType())
                            .fileExtension(a.getFileExtension())
                            .fileSize(a.getFileSize())
                            .createdAt(a.getMessage().getCreatedAt()).build();
        }).toList();

        Integer nextCursor=null;
        if (!attachmentSummaries.isEmpty()){
            nextCursor=attachmentSummaries.get(attachmentSummaries.size() - 1).getMessageId();
        }

        ChatAttachmentSlice attachmentSlice=ChatAttachmentSlice.builder()
                .attachments(attachmentSummaries).hasNext(slice.hasNext()).nextCursor(nextCursor).build();

        return attachmentSlice;
    }

    public List<ChatAttachmentDto> uploadFiles(List<MultipartFile> files){
        if (files == null || files.isEmpty()){
            throw new RuntimeException("업로드 할 파일이 없습니다.");
        }

        List<ChatAttachmentDto> result=new ArrayList<>();

        for (MultipartFile file:files){
            if (file == null || file.isEmpty()){
                continue;
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
                throw new RuntimeException("파일 저장 중 오류가 발생했습니다. ==> " + originalFileName, e);
            }

            String fileUrl="/upload/chat/" + storedFileName;

            result.add(ChatAttachmentDto.builder()
                    .originalFileName(originalFileName)
                    .storedFileName(storedFileName)
                    .fileUrl(fileUrl)
                    .contentType(contentType)
                    .fileExtension(fileExtension)
                    .fileSize(fileSize)
                    .thumbnailUrl(null)
                    .build());
        }

        return result;
    }

    private String getFileExtension(String fileName){
        int lastDotIndex=fileName.lastIndexOf(".");
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1){
            return "";
        }

        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
