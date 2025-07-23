package com.example.hexagonalorders.infrastructure.out.persistence.repository;

import com.example.hexagonalorders.domain.model.OutboxMessage;
import com.example.hexagonalorders.domain.model.OutboxMessage.Status;
import com.example.hexagonalorders.domain.port.out.OutboxRepository;
import com.example.hexagonalorders.infrastructure.out.persistence.entity.OutboxJpaEntity;
import com.example.hexagonalorders.infrastructure.out.persistence.entity.OutboxJpaEntity.OutboxStatusJpa;
import com.example.hexagonalorders.infrastructure.out.persistence.mapper.OutboxMessageMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class OutboxRepositoryAdapter implements OutboxRepository {

    private final OutboxMessageJpaRepository jpaRepository;

    public OutboxRepositoryAdapter(OutboxMessageJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void save(OutboxMessage message) {
        OutboxJpaEntity entity = OutboxMessageMapper.toEntity(message);
        jpaRepository.save(entity);
    }

    @Override
    public List<OutboxMessage> findPending(int limit) {
        return jpaRepository
            .findTop10ByStatusOrderByCreatedAtAsc(OutboxStatusJpa.PENDING, PageRequest.of(0, limit))
            .stream()
            .map(OutboxMessageMapper::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public void markProcessed(UUID id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(OutboxStatusJpa.PROCESSED);
            entity.setProcessedAt(java.time.Instant.now());
            jpaRepository.save(entity);
        });
    }

    @Override
    public void markFailed(UUID id) {
        jpaRepository.findById(id).ifPresent(entity -> {
            entity.setStatus(OutboxStatusJpa.FAILED);
            jpaRepository.save(entity);
        });
    }
}
