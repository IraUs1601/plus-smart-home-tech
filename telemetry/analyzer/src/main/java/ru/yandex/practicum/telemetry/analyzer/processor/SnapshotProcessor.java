package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.deserialization.SensorsSnapshotDeserializer;
import ru.yandex.practicum.telemetry.analyzer.service.SnapshotHandler;

import java.time.Duration;
import java.util.List;
import java.util.Properties;

@Service
@RequiredArgsConstructor
public class SnapshotProcessor {
    private KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final SnapshotHandler snapshotHandler;
    private volatile boolean running = true;

    @Value("${kafka.topics.snapshots}")
    private String snapshotsTopic;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id.snapshots:analyzer.snapshots}")
    private String snapshotsGroupId;

    @PostConstruct
    public void start() {
        new Thread(this::run, "SnapshotProcessorThread").start();
    }

    public void run() {
        try {
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapServers);
            props.put("group.id", snapshotsGroupId);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", SensorsSnapshotDeserializer.class.getName());
            props.put("auto.offset.reset", "earliest");
            props.put("enable.auto.commit", "false");

            snapshotConsumer = new KafkaConsumer<>(props);
            snapshotConsumer.subscribe(List.of(snapshotsTopic));

            while (running) {
                try {
                    ConsumerRecords<String, SensorsSnapshotAvro> records =
                            snapshotConsumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                        try {
                            SensorsSnapshotAvro snapshot = record.value();
                            if (snapshot == null) {
                                continue;
                            }

                            snapshotHandler.handle(snapshot);

                        } catch (Exception e) {
                            snapshotConsumer.seek(
                                    new TopicPartition(record.topic(), record.partition()),
                                    record.offset() + 1
                            );
                        }
                    }

                    snapshotConsumer.commitSync();
                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

        } catch (Exception ignored) {
        } finally {
            shutdown();
        }
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        if (snapshotConsumer != null) {
            try {
                snapshotConsumer.wakeup();
                snapshotConsumer.close(Duration.ofSeconds(5));
            } catch (Exception ignored) {
            }
        }
    }
}