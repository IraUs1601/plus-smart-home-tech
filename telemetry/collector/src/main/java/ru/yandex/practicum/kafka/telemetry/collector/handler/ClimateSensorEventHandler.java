package ru.yandex.practicum.kafka.telemetry.collector.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.model.ClimateSensorEvent;
import ru.yandex.practicum.kafka.telemetry.collector.service.EventService;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ClimateSensorEventHandler implements SensorEventHandler {
    private final EventService sensorEventService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Handling ClimateSensorEvent: {}", event);
        ClimateSensorEvent climateEvent = new ClimateSensorEvent();
        climateEvent.setId(event.getId());
        climateEvent.setHubId(event.getHubId());
        climateEvent.setTimestamp(convertTimestamp(event.getTimestamp()));
        climateEvent.setTemperatureC(event.getClimateSensorEvent().getTemperatureC());
        climateEvent.setHumidity(event.getClimateSensorEvent().getHumidity());
        climateEvent.setCo2Level(event.getClimateSensorEvent().getCo2Level());

        sensorEventService.processEvent(climateEvent);
    }

    private Instant convertTimestamp(Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}