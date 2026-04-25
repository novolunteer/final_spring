package com.example.demo.payment;

import com.example.demo.payment.dto.PaymentConfirmDto;
import com.example.demo.payment.dto.PaymentDto;
import com.example.demo.payment.dto.PaymentPrepareDto;
import com.example.demo.security.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;

    @GetMapping("/payment")
    public ResponseEntity<Page<PaymentDto>> getPaymentList(@RequestParam(name = "keyword", required = false) String keyword,
                                                           @AuthenticationPrincipal CustomUserDetails details,
                                                           Pageable pageable){
        if (details == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer departmentId=details.getDepartmentId();
        if (departmentId == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try{
            Page<PaymentDto> payments=paymentService.getPaymentList(keyword, pageable, departmentId);
            return ResponseEntity.ok(payments);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment/prepare")
    public ResponseEntity<PaymentPrepareDto> preparePayment(@RequestBody PaymentPrepareDto dto,
                                                            @AuthenticationPrincipal CustomUserDetails details){
        if (details == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer departmentId=details.getDepartmentId();
        if (departmentId == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try{
            PaymentPrepareDto prepare=paymentService.preparePayment(dto.getBillingId(), dto.getAmount(), departmentId);
            return ResponseEntity.ok(prepare);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/payment/confirm")
    public ResponseEntity<String> confirmPayment(@RequestBody PaymentConfirmDto dto,
                                                 @AuthenticationPrincipal CustomUserDetails details){
        if (details == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Integer departmentId=details.getDepartmentId();
        if (departmentId == null){
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        try{
            paymentService.confirmPayment(dto, departmentId);
            return ResponseEntity.ok("success");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
