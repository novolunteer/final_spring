package com.example.demo.reception;

import com.example.demo.reception.dto.ReceptionResponse;
import com.example.demo.reservation.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<ReceptionResponse> receptionList(Pageable pageable){
        return receptionService.receptionList("", pageable);
    }

    @GetMapping("/api/administration/recieved")
    public Map<String,Object> receptionConfirmed(@RequestParam Integer receptionId){
        return receptionService.ReceptionReceived(receptionId);
    }

    @GetMapping("/api/reception")
    public Page<ReceptionResponse> receptionStatusList(@RequestParam(required = false) String status,
                                                  @RequestParam(required = false) String name,
                                                  Pageable pageable){
        if (status == null || status.isEmpty()) {
            return receptionService.receptionList(name, pageable);
        }else{
            return receptionService.receptionStatusList(ReceptionStatus.valueOf(status), name, pageable);
        }
    }
}
