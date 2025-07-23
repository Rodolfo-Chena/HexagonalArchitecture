package com.example.hexagonalorders.application.service;

import com.example.hexagonalorders.domain.event.DomainEvent;
import com.example.hexagonalorders.domain.model.Order;
import com.example.hexagonalorders.domain.model.OutboxMessage;
import com.example.hexagonalorders.domain.model.valueobject.OrderNumber;
import com.example.hexagonalorders.domain.port.in.OrderUseCase;
import com.example.hexagonalorders.domain.port.out.OrderNumberGenerator;
import com.example.hexagonalorders.domain.port.out.OrderRepository;
import com.example.hexagonalorders.domain.port.out.OutboxRepository;
import com.example.hexagonalorders.domain.service.OrderValidationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Application service implementing the order-related use cases.
 * This class orchestrates domain logic and coordinates with output ports.
 * It is part of the application layer and is responsible for:
 * - Implementing use cases defined by the domain
 * - Coordinating between domain objects and services
 * - Managing transactions and use case flow
 */
@RequiredArgsConstructor
public class OrderService implements OrderUseCase {

    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;
    private final OrderValidationService orderValidationService;
    private final ApplicationEventPublisher eventPublisher;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public Order createOrder(Order order) {
        // Validate the order using the domain service
        orderValidationService.validateOrder(order);

        OrderNumber orderNumber = orderNumberGenerator.generate();
        Order orderWithNumber = new Order(
            orderNumber,
            order.getCustomerId(),
            order.getOrderDate(),
            order.getItems(),
            order.getStatus()
        );

        Order savedOrder = orderRepository.save(orderWithNumber);

        // Publicar y guardar eventos en outbox
        for (DomainEvent event : savedOrder.getDomainEvents()) {
            eventPublisher.publishEvent(event);
            persistToOutbox(event, "Order", orderNumber.value());
        }

        savedOrder.clearDomainEvents();
        return savedOrder;
    }

    @Override
    public Optional<Order> getOrder(OrderNumber orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    @Override
    public void deleteOrder(OrderNumber orderNumber) {
        orderRepository.deleteByOrderNumber(orderNumber);
    }

    /**
     * Persiste un evento de dominio en la tabla outbox.
     */
    private void persistToOutbox(DomainEvent event, String aggregateType, String aggregateId) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String eventType = event.getClass().getSimpleName();
            UUID uuid = UUID.nameUUIDFromBytes(aggregateId.getBytes());

            OutboxMessage outboxMessage = OutboxMessage.createPendingMessage(
                aggregateType, uuid, eventType, payload
            );

            outboxRepository.save(outboxMessage);
        } catch (Exception e) {
            throw new RuntimeException("Error al persistir evento en outbox", e);
        }
    }
}
