package com.example.hexagonalorders.infrastructure.out.persistence.mapper;

import com.example.hexagonalorders.domain.model.OutboxMessage;
import com.example.hexagonalorders.infrastructure.out.persistence.entity.OutboxJpaEntity;
import com.example.hexagonalorders.infrastructure.out.persistence.entity.OutboxJpaEntity.OutboxStatusJpa;

public class OutboxMessageMapper {

    public static OutboxJpaEntity toEntity(OutboxMessage message) {
        OutboxJpaEntity entity = new OutboxJpaEntity();
        entity.setId(message.id());
        entity.setAggregateType(message.aggregateType());
        entity.setAggregateId(message.aggregateId());
        entity.setEventType(message.eventType());
        entity.setPayload(message.payload());
        entity.setStatus(OutboxStatusJpa.valueOf(message.status().name()));
        entity.setCreatedAt(message.createdAt());
        entity.setProcessedAt(message.processedAt());
        return entity;
    }

    public static OutboxMessage toDomain(OutboxJpaEntity entity) {
        return new OutboxMessage(
            entity.getId(),
            entity.getAggregateType(),
            entity.getAggregateId(),
            entity.getEventType(),
            entity.getPayload(),
            OutboxMessage.Status.valueOf(entity.getStatus().name()),
            entity.getCreatedAt(),
            entity.getProcessedAt()
        );
    }
}
