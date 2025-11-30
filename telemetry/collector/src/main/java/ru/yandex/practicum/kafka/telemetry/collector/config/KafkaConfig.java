package ru.yandex.practicum.kafka.telemetry.collector.config;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ru.yandex.practicum.kafka.telemetry.collector.serialization.AvroSerializer;

import jakarta.annotation.PostConstruct;
import java.util.Properties;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    String bootstrapServers;

    @Getter
    @Value("${kafka.sensor-events-topic}")
    String sensorEventsTopic;

    @Getter
    @Value("${kafka.hub-events-topic}")
    String hubEventsTopic;

    @Getter
    KafkaProducer<String, SpecificRecordBase> producer;

    @PostConstruct
    public void init() {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, AvroSerializer.class.getName());
        producer = new KafkaProducer<>(props);
    }
}