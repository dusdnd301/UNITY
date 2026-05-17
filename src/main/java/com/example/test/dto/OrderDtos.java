package com.example.test.dto;

import com.example.test.domain.OrderEntity;
import com.example.test.domain.OrderItemEntity;
import com.example.test.domain.OrderStatus;
import com.example.test.domain.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public final class OrderDtos {
    private OrderDtos() {
    }

    public record OrderItemRequest(@NotNull Long menuId, @Min(1) int quantity) {
    }

    public record CreateOrderRequest(@NotNull Long tableId, @NotEmpty @Valid List<OrderItemRequest> items) {
    }

    public record OrderItemResponse(Long id, Long menuId, String menuName, int quantity, int price) {
        public static OrderItemResponse from(OrderItemEntity item) {
            return new OrderItemResponse(
                    item.getId(),
                    item.getMenu().getId(),
                    item.getMenu().getName(),
                    item.getQuantity(),
                    item.getPrice()
            );
        }
    }

    public record OrderResponse(
            Long id,
            Long tableId,
            Integer tableNumber,
            String orderNumber,
            int totalPrice,
            OrderStatus status,
            PaymentStatus paymentStatus,
            LocalDateTime createdAt,
            List<OrderItemResponse> items
    ) {
        public static OrderResponse from(OrderEntity order) {
            return new OrderResponse(
                    order.getId(),
                    order.getTable().getId(),
                    order.getTable().getTableNumber(),
                    order.getOrderNumber(),
                    order.getTotalPrice(),
                    order.getStatus(),
                    order.getPaymentStatus(),
                    order.getCreatedAt(),
                    order.getItems().stream().map(OrderItemResponse::from).toList()
            );
        }
    }

    public record UpdateOrderStatusRequest(@NotNull OrderStatus status) {
    }
}
