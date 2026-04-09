package com.example.demo.payment;

import com.example.demo.billing.Billing;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {
    List<Payment> findByBilling(Billing billing);
}
