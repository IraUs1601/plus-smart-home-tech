package ru.yandex.practicum.kafka.telemetry.aggregator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.aggregator.service.SnapshotService;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.io.Closeable;
import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter implements ApplicationRunner, Closeable {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final Producer<String, SensorsSnapshotAvro> producer;
    private final SnapshotService snapshotService;

    private volatile boolean running = true;

    @Value("${kafka.sensor-events-topic}")
    private String sensorEventsTopic;

    @Value("${kafka.snapshots-topic}")
    private String snapshotsTopic;

    @Override
    public void run(ApplicationArguments args) {
        start();
    }

    public void start() {
        try {
            log.info("Subscribing to topic: {}", sensorEventsTopic);
            consumer.subscribe(Collections.singletonList(sensorEventsTopic));

            while (running) {
                ConsumerRecords<String, SensorEventAvro> records =
                        consumer.poll(Duration.ofMillis(100));

                if (records.isEmpty()) {
                    log.debug("No records received from topic {}", sensorEventsTopic);
                    continue;
                }

                log.debug("Received {} records from topic {}", records.count(), sensorEventsTopic);

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    SensorEventAvro event = record.value();

                    log.info("Processing event: hubId={}, sensorId={}, timestamp={}",
                            event.getHubId(), event.getId(), event.getTimestamp());

                    try {
                        snapshotService.updateState(event).ifPresent(snapshot -> {
                            ProducerRecord<String, SensorsSnapshotAvro> producerRecord =
                                    new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot);

                            log.info("Sending snapshot for hub {} to topic {}",
                                    snapshot.getHubId(), snapshotsTopic);

                            producer.send(producerRecord, (metadata, exception) -> {
                                if (exception != null) {
                                    log.error("Failed to send snapshot to topic {}: {}",
                                            snapshotsTopic, exception.getMessage());
                                } else {
                                    log.debug("Snapshot sent: topic={}, partition={}, offset={}",
                                            snapshotsTopic, metadata.partition(), metadata.offset());
                                }
                            });

                            producer.flush();
                        });
                    } catch (Exception e) {
                        log.error("Error processing event: hubId={}, sensorId={}",
                                event.getHubId(), event.getId(), e);
                    }
                }

                try {
                    consumer.commitSync();
                    log.debug("Offset commit completed");
                } catch (Exception e) {
                    log.error("Offset commit failed", e);
                }
            }
        } catch (WakeupException ignored) {
            log.info("Shutdown signal received");
        } catch (Exception e) {
            log.error("Critical error while processing events", e);
        } finally {
            close();
        }
    }

    @Override
    public void close() {
        running = false;
        log.info("Shutting down AggregationStarter");

        try {
            producer.flush();
            log.info("Producer flushed pending records");

            consumer.commitSync();
            log.info("Final offset commit completed");
        } catch (Exception e) {
            log.error("Error during shutdown", e);
        } finally {
            consumer.close();
            log.info("Consumer closed");

            producer.close();
            log.info("Producer closed");
        }
    }
}