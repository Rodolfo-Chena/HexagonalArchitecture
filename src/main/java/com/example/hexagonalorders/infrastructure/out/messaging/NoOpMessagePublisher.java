package com.example.hexagonalorders.infrastructure.out.messaging;

import com.example.hexagonalorders.domain.port.out.MessagePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NoOpMessagePublisher implements MessagePublisher {

    private static final Logger logger = LoggerFactory.getLogger(NoOpMessagePublisher.class);

    @Override
    public void publish(String topic, String payload) {
        logger.info("[NO-OP PUBLISHER] Publicando evento en tópico '{}': {}", topic, payload);
    }
}
