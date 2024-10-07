package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class ConfirmationCodeProducer {

    private static final Logger logger = Logger.getLogger(ConfirmationCodeProducer.class.getName());

    @Value("${spring.kafka.topic.confirmation}")
    private String confirmationTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendConfirmationCode(String email, String code) {
        logger.log(Level.INFO, "sending a message...");
        kafkaTemplate.send(confirmationTopic, email, code);
    }
}
