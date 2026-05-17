package com.example.test.repository;

import com.example.test.domain.TableEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TableRepository extends JpaRepository<TableEntity, Long> {
    Optional<TableEntity> findByTableNumber(Integer tableNumber);
    boolean existsByTableNumber(Integer tableNumber);
}
