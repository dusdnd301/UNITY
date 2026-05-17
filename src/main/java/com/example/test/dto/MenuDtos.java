package com.example.test.dto;

import com.example.test.domain.MenuEntity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public final class MenuDtos {
    private MenuDtos() {
    }

    public record MenuResponse(
            Long id,
            String name,
            String description,
            int price,
            String imageUrl,
            String category,
            boolean soldOut,
            boolean active
    ) {
        public static MenuResponse from(MenuEntity menu) {
            return new MenuResponse(
                    menu.getId(),
                    menu.getName(),
                    menu.getDescription(),
                    menu.getPrice(),
                    menu.getImageUrl(),
                    menu.getCategory(),
                    menu.isSoldOut(),
                    menu.isActive()
            );
        }
    }

    public record UpsertMenuRequest(
            @NotBlank String name,
            String description,
            @Min(0) int price,
            String imageUrl,
            @NotBlank String category,
            boolean soldOut,
            boolean active
    ) {
    }

    public record ImageUploadResponse(String imageUrl) {
    }
}
