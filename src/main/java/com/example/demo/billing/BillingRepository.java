package com.example.demo.billing;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillingRepository extends JpaRepository<Billing, Integer> {
    Page<Billing> findByRecord_RecordId(Integer recordId, Pageable pageable);
    Page<Billing> findByRecord_Patient_NameContaining(String keyword, Pageable pageable);
}
