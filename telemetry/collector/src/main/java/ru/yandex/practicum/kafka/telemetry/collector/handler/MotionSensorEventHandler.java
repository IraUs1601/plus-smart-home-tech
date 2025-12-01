package ru.yandex.practicum.kafka.telemetry.collector.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.model.MotionSensorEvent;
import ru.yandex.practicum.kafka.telemetry.collector.service.EventService;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class MotionSensorEventHandler implements SensorEventHandler {
    private final EventService sensorEventService;

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR_EVENT;
    }

    @Override
    public void handle(SensorEventProto event) {
        log.info("Handling MotionSensorEvent: {}", event);
        MotionSensorEvent motionEvent = new MotionSensorEvent();
        motionEvent.setId(event.getId());
        motionEvent.setHubId(event.getHubId());
        motionEvent.setTimestamp(convertTimestamp(event.getTimestamp()));
        motionEvent.setLinkQuality(event.getMotionSensorEvent().getLinkQuality());
        motionEvent.setMotion(event.getMotionSensorEvent().getMotion());
        motionEvent.setVoltage(event.getMotionSensorEvent().getVoltage());

        sensorEventService.processEvent(motionEvent);
    }

    private Instant convertTimestamp(Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}