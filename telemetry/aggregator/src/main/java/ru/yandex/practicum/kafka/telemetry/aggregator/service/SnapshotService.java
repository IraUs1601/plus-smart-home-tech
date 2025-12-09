package ru.yandex.practicum.kafka.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = event.getHubId();
        String sensorId = event.getId();
        Instant eventTs = event.getTimestamp();
        long eventTsMillis = eventTs.toEpochMilli();

        log.debug("Updating snapshot: hubId={}, sensorId={}, ts={}", hubId, sensorId, eventTsMillis);

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(hubId, id -> {
            log.info("Creating new snapshot for hub: {}", hubId);
            SensorsSnapshotAvro snap = new SensorsSnapshotAvro();
            snap.setHubId(hubId);
            snap.setTimestamp(eventTs);
            snap.setSensorsState(new HashMap<>());
            return snap;
        });

        if (snapshot.getSensorsState() == null) {
            log.warn("sensorsState was null for hub {}, initializing", hubId);
            snapshot.setSensorsState(new HashMap<>());
        }

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(sensorId);

        if (oldState != null) {
            Instant oldTs = oldState.getTimestamp();
            long oldTsMillis = oldTs.toEpochMilli();

            if (oldTs.isAfter(eventTs)) {
                log.debug("Skipping outdated event: oldTs={}, newTs={}", oldTs, eventTs);
                return Optional.empty();
            }

            Object newPayload = event.getPayload();
            Object oldPayload = oldState.getData();

            if (Objects.equals(oldPayload, newPayload)) {
                log.debug("No changes detected, skipping. sensorId={}", sensorId);
                return Optional.empty();
            }

            log.debug(
                    "Updating sensor state {}: oldTs={}, newTs={}",
                    sensorId, oldTsMillis, eventTsMillis
            );

        } else {
            log.debug("Adding new sensor to snapshot: {}, payload={}", sensorId, event.getPayload());
        }

        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(eventTs);
        newState.setData(event.getPayload());

        sensorsState.put(sensorId, newState);
        snapshot.setTimestamp(eventTs);

        log.info("Snapshot updated for hub {}: sensorId={}, ts={}", hubId, sensorId, eventTsMillis);
        return Optional.of(snapshot);
    }
}