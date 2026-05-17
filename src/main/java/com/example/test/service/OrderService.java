package com.example.test.service;

import com.example.test.domain.MenuEntity;
import com.example.test.domain.OrderEntity;
import com.example.test.domain.OrderItemEntity;
import com.example.test.domain.OrderStatus;
import com.example.test.domain.TableEntity;
import com.example.test.dto.OrderDtos.CreateOrderRequest;
import com.example.test.dto.OrderDtos.OrderItemRequest;
import com.example.test.dto.OrderDtos.OrderResponse;
import com.example.test.exception.ApiException;
import com.example.test.repository.MenuRepository;
import com.example.test.repository.OrderRepository;
import com.example.test.repository.TableRepository;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final List<OrderStatus> CLOSED_TABLE_STATUSES = List.of(OrderStatus.CANCELLED, OrderStatus.SETTLED);

    private final OrderRepository orderRepository;
    private final TableRepository tableRepository;
    private final MenuRepository menuRepository;
    private final RealtimeEventService realtimeEventService;

    public OrderService(OrderRepository orderRepository, TableRepository tableRepository, MenuRepository menuRepository,
                        RealtimeEventService realtimeEventService) {
        this.orderRepository = orderRepository;
        this.tableRepository = tableRepository;
        this.menuRepository = menuRepository;
        this.realtimeEventService = realtimeEventService;
    }

    @Transactional
    public OrderEntity createPendingOrder(CreateOrderRequest request) {
        TableEntity table = tableRepository.findById(request.tableId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "테이블을 찾을 수 없습니다."));

        int totalPrice = 0;
        OrderEntity order = new OrderEntity(table, generateOrderNumber(), 0);
        for (OrderItemRequest itemRequest : request.items()) {
            MenuEntity menu = menuRepository.findById(itemRequest.menuId())
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "메뉴를 찾을 수 없습니다."));
            if (!menu.isActive() || menu.isSoldOut()) {
                throw new ApiException(HttpStatus.BAD_REQUEST, menu.getName() + " 메뉴는 주문할 수 없습니다.");
            }
            int linePrice = menu.getPrice() * itemRequest.quantity();
            totalPrice += linePrice;
            order.addItem(new OrderItemEntity(menu, itemRequest.quantity(), menu.getPrice()));
        }
        if (totalPrice < 1) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "주문 금액이 올바르지 않습니다.");
        }
        OrderEntity pricedOrder = new OrderEntity(table, order.getOrderNumber(), totalPrice);
        order.getItems().forEach(pricedOrder::addItem);
        return orderRepository.save(pricedOrder);
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        OrderEntity order = createPendingOrder(request);
        OrderResponse response = OrderResponse.from(order);
        realtimeEventService.publishOrderChanged(response);
        return response;
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long id) {
        return OrderResponse.from(findOrderWithItems(id));
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByTable(Long tableId) {
        return orderRepository.findByTableIdOrderByCreatedAtDesc(tableId).stream().map(OrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOpenOrdersByTable(Long tableId) {
        return orderRepository.findByTableIdAndStatusNotInOrderByCreatedAtDesc(tableId, CLOSED_TABLE_STATUSES)
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAdminOrders() {
        return orderRepository.findByCreatedAtAfterAndStatusNotInOrderByCreatedAtDesc(
                        LocalDateTime.now().minusDays(1),
                        CLOSED_TABLE_STATUSES
                )
                .stream()
                .map(OrderResponse::from)
                .toList();
    }

    @Transactional
    public OrderResponse changeStatus(Long id, OrderStatus status) {
        OrderEntity order = findOrderWithItems(id);
        order.changeStatus(status);
        OrderResponse response = OrderResponse.from(order);
        realtimeEventService.publishOrderChanged(response);
        return response;
    }

    @Transactional
    public void settleTable(Long tableId) {
        if (!tableRepository.existsById(tableId)) {
            throw new ApiException(HttpStatus.NOT_FOUND, "테이블을 찾을 수 없습니다.");
        }
        List<OrderEntity> orders = orderRepository.findByTableIdAndStatusNotInOrderByCreatedAtDesc(tableId, CLOSED_TABLE_STATUSES);
        for (OrderEntity order : orders) {
            order.settle();
            realtimeEventService.publishOrderChanged(OrderResponse.from(order));
        }
    }

    public OrderEntity findOrderByOrderNumber(String orderNumber) {
        return orderRepository.findWithItemsByOrderNumber(orderNumber)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
    }

    private OrderEntity findOrderWithItems(Long id) {
        return orderRepository.findWithItemsById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."));
    }

    private String generateOrderNumber() {
        String prefix = "F" + LocalDate.now().format(DATE_FORMAT);
        for (int i = 0; i < 10; i++) {
            String candidate = prefix + "-" + (1000 + RANDOM.nextInt(9000));
            if (orderRepository.findByOrderNumber(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("주문번호 생성에 실패했습니다.");
    }
}
