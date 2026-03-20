package com.card.payment.pos.entity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentHistoryRepository extends JpaRepository<PaymentHistory, Long> {
    Optional<PaymentHistory> findBySystemTraceAuditNumber(String systemTraceAuditNumber);
}