package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.deserialization.HubEventDeserializer;
import ru.yandex.practicum.telemetry.analyzer.handler.HubEventHandler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final Set<HubEventHandler> handlers;
    private KafkaConsumer<String, HubEventAvro> hubEventConsumer;
    private volatile boolean running = true;

    @Value("${kafka.topics.hub}")
    private String hubEventsTopic;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.consumer.group-id.hub:analyzer.hubs}")
    private String hubGroupId;

    private Map<String, HubEventHandler> handlerMap;

    @PostConstruct
    public void start() {
        new Thread(this, "HubEventHandlerThread").start();
    }

    @Override
    public void run() {
        try {
            Properties props = new Properties();
            props.put("bootstrap.servers", bootstrapServers);
            props.put("group.id", hubGroupId);
            props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
            props.put("value.deserializer", HubEventDeserializer.class.getName());
            props.put("auto.offset.reset", "earliest");
            props.put("enable.auto.commit", "false");

            hubEventConsumer = new KafkaConsumer<>(props);

            handlerMap = handlers.stream()
                    .collect(Collectors.toMap(HubEventHandler::getEventType, Function.identity()));

            hubEventConsumer.subscribe(List.of(hubEventsTopic));

            while (running) {
                try {
                    ConsumerRecords<String, HubEventAvro> records =
                            hubEventConsumer.poll(Duration.ofMillis(1000));

                    for (ConsumerRecord<String, HubEventAvro> record : records) {
                        try {
                            HubEventAvro event = record.value();
                            if (event == null || event.getPayload() == null) {
                                continue;
                            }

                            String eventType = event.getPayload().getClass().getSimpleName();
                            HubEventHandler handler = handlerMap.get(eventType);
                            if (handler != null) {
                                handler.handle(event);
                            } else {
                            }

                        } catch (Exception e) {
                            hubEventConsumer.seek(
                                    new TopicPartition(record.topic(), record.partition()),
                                    record.offset() + 1
                            );
                        }
                    }

                    hubEventConsumer.commitSync();

                } catch (Exception e) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

        } catch (WakeupException ignored) {
        } catch (Exception e) {
        } finally {
            closeConsumer();
        }
    }

    @PreDestroy
    public void shutdown() {
        running = false;
        if (hubEventConsumer != null) {
            hubEventConsumer.wakeup();
        }
    }

    private void closeConsumer() {
        try {
            if (hubEventConsumer != null) {
                hubEventConsumer.commitSync();
                hubEventConsumer.close(Duration.ofSeconds(5));
            }
        } catch (Exception ignored) {
        }
    }
}