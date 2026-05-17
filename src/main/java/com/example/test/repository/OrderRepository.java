package com.example.test.repository;

import com.example.test.domain.OrderEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"table", "items", "items.menu", "payment"})
    Optional<OrderEntity> findWithItemsById(Long id);

    @EntityGraph(attributePaths = {"table", "items", "items.menu", "payment"})
    Optional<OrderEntity> findWithItemsByOrderNumber(String orderNumber);

    @EntityGraph(attributePaths = {"table", "items", "items.menu"})
    List<OrderEntity> findByTableIdOrderByCreatedAtDesc(Long tableId);

    @EntityGraph(attributePaths = {"table", "items", "items.menu", "payment"})
    List<OrderEntity> findByCreatedAtAfterOrderByCreatedAtDesc(LocalDateTime from);
}
