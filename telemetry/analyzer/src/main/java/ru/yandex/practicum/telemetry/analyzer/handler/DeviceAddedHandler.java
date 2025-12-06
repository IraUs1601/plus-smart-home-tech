package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedHandler implements HubEventHandler {
    private final SensorRepository sensorRepository;

    @Override
    public void handle(HubEventAvro event) {
        DeviceAddedEventAvro payload = (DeviceAddedEventAvro) event.getPayload();
        if (!sensorRepository.existsByIdInAndHubId(List.of(payload.getId()), event.getHubId())) {
            Sensor sensor = Sensor.builder()
                    .id(payload.getId())
                    .hubId(event.getHubId())
                    .build();
            sensorRepository.save(sensor);
            log.info("Added device {} for hub {}", payload.getId(), event.getHubId());
        } else {
            log.info("Device {} already exists for hub {}", payload.getId(), event.getHubId());
        }
    }

    @Override
    public String getEventType() {
        return DeviceAddedEventAvro.class.getSimpleName();
    }
}