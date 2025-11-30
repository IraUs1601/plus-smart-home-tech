package ru.yandex.practicum.kafka.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.kafka.telemetry.collector.model.HubEvent;
import ru.yandex.practicum.kafka.telemetry.collector.model.SensorEvent;
import ru.yandex.practicum.kafka.telemetry.collector.service.HubEventService;
import ru.yandex.practicum.kafka.telemetry.collector.service.SensorEventService;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Slf4j
public class CollectorController {

    private final SensorEventService sensorEventService;
    private final HubEventService hubEventService;

    @PostMapping("/sensors")
    public ResponseEntity<Void> collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        log.info("Received sensor event: {}", event);
        sensorEventService.processEvent(event);
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/hubs")
    public ResponseEntity<Void> collectHubEvent(@Valid @RequestBody HubEvent event) {
        log.info("Received hub event: {}", event);
        hubEventService.processEvent(event);
        return ResponseEntity.accepted().build();
    }
}