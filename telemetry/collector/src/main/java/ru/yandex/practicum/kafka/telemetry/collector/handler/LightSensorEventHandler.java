package ru.yandex.practicum.kafka.telemetry.collector.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.model.LightSensorEvent;
import ru.yandex.practicum.kafka.telemetry.collector.service.EventService;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class LightSensorEventHandler implements SensorEventHandler {
    private final EventService sensorEventService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Handling LightSensorEvent: {}", event);
        LightSensorEvent lightEvent = new LightSensorEvent();
        lightEvent.setId(event.getId());
        lightEvent.setHubId(event.getHubId());
        lightEvent.setTimestamp(convertTimestamp(event.getTimestamp()));
        lightEvent.setLinkQuality(event.getLightSensorEvent().getLinkQuality());
        lightEvent.setLuminosity(event.getLightSensorEvent().getLuminosity());

        sensorEventService.processEvent(lightEvent);
    }

    private Instant convertTimestamp(Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}