package com.example.test.repository;

import com.example.test.domain.PaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    boolean existsByTossPaymentKey(String tossPaymentKey);
    Optional<PaymentEntity> findByTossPaymentKey(String tossPaymentKey);
}
