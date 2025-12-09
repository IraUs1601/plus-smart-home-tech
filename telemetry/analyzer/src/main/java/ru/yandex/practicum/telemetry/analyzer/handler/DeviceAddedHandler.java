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

        if (payload == null) {
            return;
        }

        boolean exists = sensorRepository.existsByIdInAndHubId(
                List.of(payload.getId()),
                event.getHubId()
        );

        if (!exists) {
            Sensor sensor = Sensor.builder()
                    .id(payload.getId())
                    .hubId(event.getHubId())
                    .build();
            sensorRepository.save(sensor);
        }
    }

    @Override
    public String getEventType() {
        return DeviceAddedEventAvro.class.getSimpleName();
    }
}