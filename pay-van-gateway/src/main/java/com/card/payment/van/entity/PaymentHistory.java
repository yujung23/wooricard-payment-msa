package com.card.payment.van.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "van_payment_history")
public class PaymentHistory {

    @Id // DB가 부여하는 일련번호
    @GeneratedValue(strategy = GenerationType.IDENTITY) // DB가 부여하는 일련번호
    private Long id;

    @Column(nullable = false, unique = true)
    private String approvalId;    // VAN사(우리)가 생성한 승인번호

    @Column(nullable = false)
    private String posOrderId;    // POS 서비스의 주문 번호

    private String merchantId;    // 가맹점 ID
    private Long amount;          // 결제 금액
    private String status;        // SUCCESS, FAIL 등
    private String cardCompany;   // 카드사 정보
    private LocalDateTime createdAt;

    @Builder
    public PaymentHistory(String approvalId, String posOrderId, String merchantId,
                          Long amount, String status, String cardCompany) {
        this.approvalId = approvalId;
        this.posOrderId = posOrderId;
        this.merchantId = merchantId;
        this.amount = amount;
        this.status = status;
        this.cardCompany = cardCompany;
        this.createdAt = LocalDateTime.now();
    }
}