package ru.yandex.practicum.kafka.telemetry.collector.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.model.DeviceAddedEvent;
import ru.yandex.practicum.kafka.telemetry.collector.model.enums.DeviceType;
import ru.yandex.practicum.kafka.telemetry.collector.service.EventService;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceAddedEventHandler implements HubEventHandler {
    private final EventService hubEventService;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Handling DeviceAddedEvent: {}", event);
        DeviceAddedEvent deviceEvent = new DeviceAddedEvent();
        deviceEvent.setHubId(event.getHubId());
        deviceEvent.setTimestamp(convertTimestamp(event.getTimestamp()));
        deviceEvent.setId(event.getDeviceAdded().getId());
        deviceEvent.setDeviceType(DeviceType.valueOf(event.getDeviceAdded().getType().name()));

        hubEventService.processEvent(deviceEvent);
    }

    private Instant convertTimestamp(Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}