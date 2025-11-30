package ru.yandex.practicum.kafka.telemetry.collector.service;

public interface EventService<T> {
    void processEvent(T event);
}