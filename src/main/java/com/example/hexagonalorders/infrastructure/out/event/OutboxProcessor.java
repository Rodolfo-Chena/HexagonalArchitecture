package com.example.hexagonalorders.infrastructure.out.event;

import com.example.hexagonalorders.domain.model.OutboxMessage;
import com.example.hexagonalorders.domain.port.out.MessagePublisher;
import com.example.hexagonalorders.domain.port.out.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class OutboxProcessor {

    private static final Logger logger = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxRepository outboxRepository;
    private final MessagePublisher messagePublisher;

    public OutboxProcessor(OutboxRepository outboxRepository, MessagePublisher messagePublisher) {
        this.outboxRepository = outboxRepository;
        this.messagePublisher = messagePublisher;
    }

    @Scheduled(fixedDelayString = "${outbox.poll.ms:1000}")
    @Transactional
    public void process() {
        logger.info("[OutboxProcessor] Iniciando procesamiento de mensajes PENDING...");
        List<OutboxMessage> messages = outboxRepository.findPending(10);

        for (OutboxMessage message : messages) {
            try {
                messagePublisher.publish(message.eventType(), message.payload());
                outboxRepository.markProcessed(message.id());
                logger.info("[OutboxProcessor] Mensaje procesado: {}", message.id());
            } catch (Exception e) {
                outboxRepository.markFailed(message.id());
                logger.error("[OutboxProcessor] Falló el procesamiento del mensaje {}: {}", message.id(), e.getMessage());
            }
        }
    }
}
