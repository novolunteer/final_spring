package com.example.demo.billing;

import com.example.demo.billing.dto.BillingDto;
import com.example.demo.department.Department;
import com.example.demo.department.DepartmentRepository;
import com.example.demo.payment.dto.PaymentPrepareDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BillingService {
    private final BillingRepository billingRepository;
    private final DepartmentRepository departmentRepository;

    public Page<BillingDto> getBillingList(String keyword, Pageable pageable, Integer departmentId){
        Department billingDept=departmentRepository.findByDepartmentName("원무과")
                .orElseThrow(()->new RuntimeException("존재하지 않는 부서입니다."));

        if (!billingDept.getDepartmentId().equals(departmentId)){
            throw new RuntimeException("접근 권한이 없습니다.");
        }

        Page<Billing> billings;

        if (keyword == null || keyword.trim().isEmpty()){
            billings=billingRepository.findAll(pageable);
        } else {
            String trimmedKeyword=keyword.trim();

            if (trimmedKeyword.matches("\\d+")){
                billings=billingRepository.findByReception_ReceptionId(Integer.valueOf(trimmedKeyword), pageable);
            } else {
                billings=billingRepository.findByReception_Reservation_Patient_NameContaining(trimmedKeyword, pageable);
            }
        }

        return billings.map(b -> BillingDto.builder()
                .billingId(b.getBillingId())
                .receptionId(b.getReception().getReceptionId())
                .patientName(b.getReception().getReservation().getPatient().getName())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus().name()).build());
    }
}
