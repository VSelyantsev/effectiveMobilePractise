package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfirmationCodeProducer {

    @Value("${spring.kafka.topic.confirmation}")
    private String confirmationTopic;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendConfirmationCode(String email, String code) {
        kafkaTemplate.send(confirmationTopic, email, code);
    }
}
