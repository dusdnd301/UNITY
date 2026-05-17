package com.example.test.dto;

import com.example.test.domain.TableEntity;
import jakarta.validation.constraints.Min;

public final class TableDtos {
    private TableDtos() {
    }

    public record CreateTableRequest(@Min(1) int tableNumber, String baseUrl) {
    }

    public record RegenerateQrRequest(String baseUrl) {
    }

    public record TableResponse(Long id, Integer tableNumber, String qrCodeUrl) {
        public static TableResponse from(TableEntity table) {
            return new TableResponse(table.getId(), table.getTableNumber(), table.getQrCodeUrl());
        }
    }
}
