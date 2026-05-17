package com.example.test.dto;

import jakarta.validation.constraints.NotBlank;

public final class AdminDtos {
    private AdminDtos() {
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {
    }

    public record LoginResponse(String accessToken, String tokenType) {
    }
}
