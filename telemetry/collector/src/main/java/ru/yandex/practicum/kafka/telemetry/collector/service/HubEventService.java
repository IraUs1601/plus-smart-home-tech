package ru.yandex.practicum.kafka.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.collector.mapper.HubEventMapper;
import ru.yandex.practicum.kafka.telemetry.collector.model.HubEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class HubEventService implements EventService<HubEvent> {
    private final KafkaConfig kafkaConfig;

    @Override
    public void processEvent(HubEvent event) {
        var avroEvent = HubEventMapper.toAvro(event);
        kafkaConfig.getProducer().send(new ProducerRecord<>(kafkaConfig.getHubEventsTopic(), event.getHubId(), avroEvent));
        log.info("Sent hub event to Kafka topic {}: {}", kafkaConfig.getHubEventsTopic(), event);
    }
}