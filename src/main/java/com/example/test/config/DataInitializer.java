package com.example.test.config;

import com.example.test.domain.AdminEntity;
import com.example.test.domain.AdminRole;
import com.example.test.domain.MenuEntity;
import com.example.test.repository.AdminRepository;
import com.example.test.repository.MenuRepository;
import com.example.test.repository.TableRepository;
import com.example.test.service.TableService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner initializeData(
            AdminRepository adminRepository,
            MenuRepository menuRepository,
            TableRepository tableRepository,
            TableService tableService,
            PasswordEncoder passwordEncoder,
            @Value("${ADMIN_USERNAME:admin}") String adminUsername,
            @Value("${ADMIN_PASSWORD:admin1234}") String adminPassword
    ) {
        return args -> {
            if (adminRepository.findByUsername(adminUsername).isEmpty()) {
                adminRepository.save(new AdminEntity(adminUsername, passwordEncoder.encode(adminPassword), AdminRole.ROLE_ADMIN));
            }
            if (menuRepository.count() == 0) {
                menuRepository.save(new MenuEntity("김치전", "바삭하게 구운 축제 대표 메뉴", 12000, "/images/menu-placeholder.svg", "안주"));
                menuRepository.save(new MenuEntity("어묵탕", "따뜻한 국물 안주", 10000, "/images/menu-placeholder.svg", "탕"));
                menuRepository.save(new MenuEntity("콜라", "355ml 캔", 2000, "/images/menu-placeholder.svg", "음료"));
            }
            if (tableRepository.count() == 0) {
                tableService.createTable(1);
                tableService.createTable(2);
                tableService.createTable(3);
            }
        };
    }
}
