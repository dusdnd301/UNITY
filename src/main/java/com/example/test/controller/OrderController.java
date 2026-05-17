package com.example.test.controller;

import com.example.test.dto.OrderDtos.CreateOrderRequest;
import com.example.test.dto.OrderDtos.OrderResponse;
import com.example.test.dto.OrderDtos.UpdateOrderStatusRequest;
import com.example.test.service.OrderService;
import com.example.test.service.RealtimeEventService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService orderService;
    private final RealtimeEventService realtimeEventService;

    public OrderController(OrderService orderService, RealtimeEventService realtimeEventService) {
        this.orderService = orderService;
        this.realtimeEventService = realtimeEventService;
    }

    @PostMapping
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) {
        return orderService.getOrder(id);
    }

    @GetMapping("/table/{tableId}")
    public List<OrderResponse> getOrdersByTable(@PathVariable Long tableId) {
        return orderService.getOrdersByTable(tableId);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.changeStatus(id, request.status());
    }

    @GetMapping("/{id}/stream")
    public SseEmitter streamOrder(@PathVariable Long id) {
        orderService.getOrder(id);
        return realtimeEventService.subscribeOrder(id);
    }
}
