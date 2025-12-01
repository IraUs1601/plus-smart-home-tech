package ru.yandex.practicum.kafka.telemetry.collector.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.model.SwitchSensorEvent;
import ru.yandex.practicum.kafka.telemetry.collector.service.EventService;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class SwitchSensorEventHandler implements SensorEventHandler {
    private final EventService sensorEventService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Handling SwitchSensorEvent: {}", event);
        SwitchSensorEvent switchEvent = new SwitchSensorEvent();
        switchEvent.setId(event.getId());
        switchEvent.setHubId(event.getHubId());
        switchEvent.setTimestamp(convertTimestamp(event.getTimestamp()));
        switchEvent.setState(event.getSwitchSensorEvent().getState());

        sensorEventService.processEvent(switchEvent);
    }

    private Instant convertTimestamp(Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}