package com.example.demo.reception;

import com.example.demo.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReceptionController {
    private final ReservationService reservationService;
    private final ReceptionService receptionService;

    @GetMapping("/api/administration")
    public Map<String,Object> receptionList(){
        return receptionService.receptionList();
    }

    @GetMapping("/api/administration/recieved")
    public Map<String,Object> receptionConfirmed(@RequestParam Integer receptionId){
        return receptionService.ReceptionReceived(receptionId);
    }

    @GetMapping("/api/medicalrecord")
    public Map<String,Object> receptionPendingList(){
        return receptionService.receptionPendingList();
    }
}
