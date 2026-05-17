package com.example.test.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments", indexes = {
        @Index(name = "idx_toss_payment_key", columnList = "tossPaymentKey", unique = true)
})
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private OrderEntity order;

    @Column(nullable = false, unique = true)
    private String tossPaymentKey;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private String method;

    @Column(nullable = false)
    private String status;

    private LocalDateTime approvedAt;

    protected PaymentEntity() {
    }

    public PaymentEntity(OrderEntity order, String tossPaymentKey, int amount, String method, String status, LocalDateTime approvedAt) {
        this.order = order;
        this.tossPaymentKey = tossPaymentKey;
        this.amount = amount;
        this.method = method;
        this.status = status;
        this.approvedAt = approvedAt;
    }

    public Long getId() {
        return id;
    }

    public OrderEntity getOrder() {
        return order;
    }

    public String getTossPaymentKey() {
        return tossPaymentKey;
    }

    public int getAmount() {
        return amount;
    }

    public String getMethod() {
        return method;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }
}
