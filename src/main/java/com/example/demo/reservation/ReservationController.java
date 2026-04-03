package com.example.demo.reservation;

import com.example.demo.reservation.dto.ReservationDto;
import com.example.demo.reservation.dto.ReservationResponse;
import com.example.demo.security.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
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

    @PostMapping("/api/reservation/confirm")
    public Map<String,Object> reservationConfirmed(@RequestBody ReservationDto reservationDto){
        Map<String,Object> map=new HashMap<>();

        Integer reservationId=null;
        if(reservationDto.getDoctorId() != null){
            reservationId=reservationService.reservationConfirmed(reservationDto);
        }else{
            reservationId=reservationService.reservationPending(reservationDto);
        }

        map.put("reservationId",reservationId);
        return map;
    }

    @GetMapping("/api/reservation")
    public Map<String,Object> reservationList(@RequestParam(required = false) Integer department){
        Map<String,Object> map=new HashMap<>();
        List<ReservationResponse> reservation;

        if(department==null){
            reservation = reservationService.reservationList();
        }else{
            reservation = reservationService.reservationList(department);
        }
        map.put("content",reservation);

        return map;
    }

    @GetMapping("/api/reservation/pending")
    public Map<String,Object> reservationPendingList(@RequestParam(required = false) Integer department){
        Map<String,Object> map=new HashMap<>();
        List<ReservationResponse> reservation;

        if(department==null) {
            reservation=reservationService.reservationPendingList();
        }else{
            reservation=reservationService.reservationPendingList(department);
        }
        map.put("content",reservation);
        return map;
    }

    @GetMapping("/api/reservation/confirmed")
    public Map<String,Object> reservationConfirmedList(@RequestParam(required = false) Integer department){
        Map<String,Object> map=new HashMap<>();
        List<ReservationResponse> reservation;

        if(department==null) {
            reservation = reservationService.reservationconfirmedList();
        }else{
            reservation = reservationService.reservationconfirmedList(department);
        }
        map.put("content",reservation);
        return map;
    }

    @GetMapping("/api/reservation/delete")
    public Map<String, Object> reservationCancel(@RequestParam("reservationId") Integer reservationId){
        return Map.of("reservationId",reservationService.reservationCancel(reservationId));
    }

    @PutMapping("/api/reservation")
    public Map<String, Object> reservationUpdate(@RequestBody ReservationDto reservationDto){
        return Map.of("reservationId", reservationService.reservationUpdate(reservationDto));
    }
}
