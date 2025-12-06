package ru.yandex.practicum.telemetry.analyzer.processor;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.service.SnapshotHandler;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> snapshotConsumer;
    private final SnapshotHandler snapshotHandler;

    @Value("${kafka.topics.snapshots}")
    private String snapshotsTopic;

    private volatile boolean running = true;

    @PostConstruct
    public void start() {
        Thread t = new Thread(this::run, "SnapshotProcessorThread");
        t.start();
        log.info("Started SnapshotProcessor thread");
    }

    public void run() {
        try {
            snapshotConsumer.subscribe(List.of(snapshotsTopic));
            log.info("Subscribed to snapshots topic: {}", snapshotsTopic);

            while (running) {
                try {
                    ConsumerRecords<String, SensorsSnapshotAvro> records =
                            snapshotConsumer.poll(Duration.ofMillis(100));

                    for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                        try {
                            SensorsSnapshotAvro snapshot = record.value();
                            if (snapshot == null) {
                                log.warn("Null snapshot received at offset {} in topic {}",
                                        record.offset(), snapshotsTopic);
                                continue;
                            }
                            log.info("Processing snapshot: hubId={}, offset={}",
                                    snapshot.getHubId(), record.offset());
                            snapshotHandler.handle(snapshot);
                        } catch (Exception e) {
                            log.error("Failed to process snapshot at offset {} in topic {}: {}",
                                    record.offset(), snapshotsTopic, e.getMessage(), e);
                            snapshotConsumer.seek(
                                    new TopicPartition(record.topic(), record.partition()),
                                    record.offset() + 1
                            );
                        }
                    }

                    snapshotConsumer.commitSync();
                } catch (WakeupException e) {
                    if (running) {
                        log.error("WakeupException in SnapshotProcessor", e);
                    }
                } catch (Exception e) {
                    log.error("Error polling snapshots: {}", e.getMessage(), e);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            shutdownInternal();
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down SnapshotProcessor");
        running = false;
        snapshotConsumer.wakeup();
    }

    private void shutdownInternal() {
        try {
            snapshotConsumer.commitSync();
        } catch (Exception e) {
            log.warn("Error during final commit in SnapshotProcessor", e);
        }
        try {
            snapshotConsumer.close(Duration.ofSeconds(5));
            log.info("Snapshot consumer closed");
        } catch (Exception e) {
            log.error("Error closing snapshot consumer: {}", e.getMessage(), e);
        }
    }
}