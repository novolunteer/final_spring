package com.example.demo.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendMessageRequest {
    private Integer roomId;
    private String content;
    private Integer parentMessageId;
}
