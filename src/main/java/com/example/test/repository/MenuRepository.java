package com.example.test.repository;

import com.example.test.domain.MenuEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    List<MenuEntity> findByActiveTrueOrderByCategoryAscNameAsc();
}
