package ru.yandex.practicum.kafka.telemetry.collector.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.model.ScenarioRemovedEvent;
import ru.yandex.practicum.kafka.telemetry.collector.service.EventService;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedEventHandler implements HubEventHandler {
    private final EventService hubEventService;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Handling ScenarioRemovedEvent: {}", event);
        ScenarioRemovedEvent removedEvent = new ScenarioRemovedEvent();
        removedEvent.setHubId(event.getHubId());
        removedEvent.setTimestamp(convertTimestamp(event.getTimestamp()));
        removedEvent.setName(event.getScenarioRemoved().getName());

        hubEventService.processEvent(removedEvent);
    }

    private Instant convertTimestamp(Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}