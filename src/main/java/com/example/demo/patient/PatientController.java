package com.example.demo.patient;

import com.example.demo.patient.dto.MyInfoResponse;
import com.example.demo.patient.dto.MyPaymentResponse;
import com.example.demo.patient.dto.MyReceptionResponse;
import com.example.demo.patient.dto.MyReservationResponse;
import com.example.demo.security.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patient")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;

    @GetMapping("/reservation")
    public ResponseEntity<Page<MyReservationResponse>> getMyReservations(@RequestParam(name = "status", required = false) String status,
                                                                         @AuthenticationPrincipal CustomUserDetails details, Pageable pageable){
        if (details == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userId=details.getUserId();
        if (userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try{
            Page<MyReservationResponse> responses=patientService.getMyReservations(userId, status, pageable);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/reception")
    public ResponseEntity<Page<MyReceptionResponse>> getMyReceptions(@AuthenticationPrincipal CustomUserDetails details, Pageable pageable,
                                                                     @RequestParam(name = "sort") String sort){
        if (details == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userId=details.getUserId();
        if (userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try{
            Page<MyReceptionResponse> responses=patientService.getMyReceptions(userId, pageable, sort);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/payment")
    public ResponseEntity<Page<MyPaymentResponse>> getMyPaymentList(@RequestParam(name = "receptionId") Integer receptionId,
                                                                    @AuthenticationPrincipal CustomUserDetails details, Pageable pageable){
        if (details == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userId=details.getUserId();
        if (userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try{
            Page<MyPaymentResponse> responses=patientService.getMyPaymentList(userId, receptionId, pageable);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/information")
    public ResponseEntity<MyInfoResponse> getMyInformation(@AuthenticationPrincipal CustomUserDetails details){
        if (details == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer userId=details.getUserId();
        if (userId == null){
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try{
            MyInfoResponse response=patientService.getMyInformation(userId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
