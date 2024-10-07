package ru.itis.kpfu.selyantsev.service;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConfirmationCodeListener {

    @KafkaListener(topics = "${spring.kafka.topic.confirmation}", groupId = "email-service-group")
    public void listen(ConsumerRecord<String, String> record) {
        System.out.println("Received Message: " + record.key() + " " + record.value());
    }

    @KafkaListener(topics = "log-topic", groupId = "log-topic-group")
    public void listenLogTopic(ConsumerRecord<String, String> record) {
        System.out.println(record.key() + " " + record.value());
    }
}
