package com.example.demo.reservation;

import com.example.demo.security.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReservationController {
    private final ReservationService reservationService;

    @PostMapping("/api/reservation")
    public Map<String,Object> reservationInsert(@RequestBody ReservationDto reservationDto,
                                                @AuthenticationPrincipal CustomUserDetails customUserDetails){
        Map<String,Object> map=new HashMap<>();
        Integer reservationId=reservationService.reservationReceived(reservationDto, customUserDetails);
        map.put("reservationId",reservationId);
        return map;
    }
}
