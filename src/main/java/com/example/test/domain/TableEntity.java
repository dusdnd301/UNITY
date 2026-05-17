package com.example.test.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tables")
public class TableEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Integer tableNumber;

    @Column(nullable = false, length = 1000)
    private String qrCodeUrl;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected TableEntity() {
    }

    public TableEntity(Integer tableNumber, String qrCodeUrl) {
        this.tableNumber = tableNumber;
        this.qrCodeUrl = qrCodeUrl;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Integer getTableNumber() {
        return tableNumber;
    }

    public String getQrCodeUrl() {
        return qrCodeUrl;
    }

    public void updateQrCodeUrl(String qrCodeUrl) {
        this.qrCodeUrl = qrCodeUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
