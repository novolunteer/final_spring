package com.example.demo.chat.service;

import com.example.demo.chat.dto.ChatMessageDto;
import com.example.demo.chat.dto.MessageSlice;
import com.example.demo.chat.entity.ChatMessage;
import com.example.demo.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository messageRepository;

    public MessageSlice getMessages(Integer roomId, Integer cursor, int size, Integer userId){
        Pageable pageable= PageRequest.of(0, size);
        Slice<ChatMessage> slice;

        if (cursor == null){
            slice=messageRepository.findByRoom_RoomIdOrderByMessageIdDesc(roomId, pageable);
        } else {
            slice=messageRepository.findByRoom_RoomIdAndMessageIdLessThanOrderByMessageIdDesc(roomId, cursor, pageable);
        }

        List<ChatMessageDto> messages=slice.getContent().stream()
                .map(m -> ChatMessageDto.builder()
                        .messageId(m.getMessageId())
                        .roomId(m.getRoom().getRoomId())
                        .senderId(m.getUser().getUserId())
                        .content(m.getContent())
                        .messageType(m.getMessageType().name())
                        .mine(m.getUser().getUserId().equals(userId))
                        .createdAt(m.getCreatedAt())
                        .build()).toList();

        Integer nextCursor=null;
        if (!messages.isEmpty()){
            nextCursor=messages.get(messages.size() - 1).getMessageId();
        }

        return MessageSlice.builder()
                .messages(messages)
                .hasNext(slice.hasNext())
                .nextCursor(nextCursor).build();
    }
}
