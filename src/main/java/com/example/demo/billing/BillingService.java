package com.example.demo.billing;

import com.example.demo.billing.dto.BillingDto;
import com.example.demo.department.Department;
import com.example.demo.department.DepartmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
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
                billings=billingRepository.findByRecord_RecordId(Integer.valueOf(trimmedKeyword), pageable);
            } else {
                billings=billingRepository.findByRecord_Patient_NameContaining(trimmedKeyword, pageable);
            }
        }

        return billings.map(b -> BillingDto.builder()
                .billingId(b.getBillingId())
                .recordId(b.getRecord().getRecordId())
                .patientName(b.getRecord().getPatient().getName())
                .totalAmount(b.getTotalAmount())
                .status(b.getStatus().name()).build());
    }
}
