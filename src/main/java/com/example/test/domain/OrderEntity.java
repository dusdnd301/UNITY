package com.example.test.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_number", columnList = "orderNumber", unique = true),
        @Index(name = "idx_order_created_at", columnList = "createdAt")
})
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_id", nullable = false)
    private TableEntity table;

    @Column(nullable = false, unique = true)
    private String orderNumber;

    @Column(nullable = false)
    private int totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItemEntity> items = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PaymentEntity payment;

    protected OrderEntity() {
    }

    public OrderEntity(TableEntity table, String orderNumber, int totalPrice) {
        this.table = table;
        this.orderNumber = orderNumber;
        this.totalPrice = totalPrice;
        this.status = OrderStatus.PENDING;
        this.paymentStatus = PaymentStatus.READY;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public TableEntity getTable() {
        return table;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public List<OrderItemEntity> getItems() {
        return items;
    }

    public PaymentEntity getPayment() {
        return payment;
    }

    public void addItem(OrderItemEntity item) {
        items.add(item);
        item.assignOrder(this);
    }

    public void markPaid() {
        this.status = OrderStatus.PAID;
        this.paymentStatus = PaymentStatus.SUCCESS;
    }

    public void markPaymentFailed() {
        this.paymentStatus = PaymentStatus.FAILED;
    }

    public void changeStatus(OrderStatus status) {
        if (this.status == OrderStatus.CANCELLED || this.status == OrderStatus.DONE) {
            throw new IllegalStateException("완료 또는 취소된 주문은 상태 변경이 불가능합니다.");
        }
        if (status == OrderStatus.PENDING && this.paymentStatus == PaymentStatus.SUCCESS) {
            throw new IllegalStateException("결제 완료 주문은 PENDING으로 되돌릴 수 없습니다.");
        }
        this.status = status;
    }
}
