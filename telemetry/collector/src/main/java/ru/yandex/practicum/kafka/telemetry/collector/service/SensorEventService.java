package ru.yandex.practicum.kafka.telemetry.collector.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.collector.config.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.collector.mapper.SensorEventMapper;
import ru.yandex.practicum.kafka.telemetry.collector.model.SensorEvent;

@Service
@RequiredArgsConstructor
@Slf4j
public class SensorEventService implements EventService<SensorEvent> {
    private final KafkaConfig kafkaConfig;

    @Override
    public void processEvent(SensorEvent event) {
        var avroEvent = SensorEventMapper.toAvro(event);
        kafkaConfig.getProducer().send(new ProducerRecord<>(kafkaConfig.getSensorEventsTopic(), event.getId(), avroEvent));
        log.info("Sent sensor event to Kafka topic {}: {}", kafkaConfig.getSensorEventsTopic(), event);
    }
}