package com.example.test.service;

import com.example.test.dto.OrderDtos.OrderResponse;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
public class RealtimeEventService {
    private final List<SseEmitter> adminEmitters = new CopyOnWriteArrayList<>();
    private final Map<Long, List<SseEmitter>> orderEmitters = new ConcurrentHashMap<>();

    public SseEmitter subscribeAdmin() {
        SseEmitter emitter = new SseEmitter(1000L * 60 * 30);
        adminEmitters.add(emitter);
        emitter.onCompletion(() -> adminEmitters.remove(emitter));
        emitter.onTimeout(() -> adminEmitters.remove(emitter));
        send(emitter, "connected", "ok");
        return emitter;
    }

    public void publishOrderChanged(OrderResponse order) {
        for (SseEmitter emitter : adminEmitters) {
            send(emitter, "order-changed", order);
        }
        for (SseEmitter emitter : orderEmitters.getOrDefault(order.id(), List.of())) {
            send(emitter, "order-changed", order);
        }
    }

    public SseEmitter subscribeOrder(Long orderId) {
        SseEmitter emitter = new SseEmitter(1000L * 60 * 30);
        orderEmitters.computeIfAbsent(orderId, key -> new CopyOnWriteArrayList<>()).add(emitter);
        emitter.onCompletion(() -> removeOrderEmitter(orderId, emitter));
        emitter.onTimeout(() -> removeOrderEmitter(orderId, emitter));
        send(emitter, "connected", "ok");
        return emitter;
    }

    private void removeOrderEmitter(Long orderId, SseEmitter emitter) {
        List<SseEmitter> emitters = orderEmitters.get(orderId);
        if (emitters != null) {
            emitters.remove(emitter);
        }
    }

    private void send(SseEmitter emitter, String event, Object data) {
        try {
            emitter.send(SseEmitter.event().name(event).data(data));
        } catch (IOException | IllegalStateException ex) {
            adminEmitters.remove(emitter);
        }
    }
}
