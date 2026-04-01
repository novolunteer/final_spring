package com.example.demo.slot.dto;

import com.example.demo.slot.SlotStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class SlotResponse {
    private Integer slotId;
    private Integer doctorId;
    private LocalDateTime startTime;
    private Integer capacity;
    private Boolean available;
    private SlotStatus type;
}
