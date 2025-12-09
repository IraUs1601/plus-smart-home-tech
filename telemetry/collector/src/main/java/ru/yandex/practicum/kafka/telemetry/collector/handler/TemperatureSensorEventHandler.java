package ru.yandex.practicum.kafka.telemetry.collector.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.model.TemperatureSensorEvent;
import ru.yandex.practicum.kafka.telemetry.collector.service.EventService;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class TemperatureSensorEventHandler implements SensorEventHandler {
    private final EventService sensorEventService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Handling TemperatureSensorEvent: {}", event);
        TemperatureSensorEvent temperatureEvent = new TemperatureSensorEvent();
        temperatureEvent.setId(event.getId());
        temperatureEvent.setHubId(event.getHubId());
        temperatureEvent.setTimestamp(convertTimestamp(event.getTimestamp()));
        temperatureEvent.setTemperatureC(event.getTemperatureSensorEvent().getTemperatureC());
        temperatureEvent.setTemperatureF(event.getTemperatureSensorEvent().getTemperatureF());

        sensorEventService.processEvent(temperatureEvent);
    }

    private Instant convertTimestamp(Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}