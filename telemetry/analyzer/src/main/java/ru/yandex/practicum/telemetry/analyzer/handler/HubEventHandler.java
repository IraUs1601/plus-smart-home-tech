package ru.yandex.practicum.telemetry.analyzer.handler;

import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;

public interface HubEventHandler {
    void handle(HubEventAvro event);
    String getEventType();
}