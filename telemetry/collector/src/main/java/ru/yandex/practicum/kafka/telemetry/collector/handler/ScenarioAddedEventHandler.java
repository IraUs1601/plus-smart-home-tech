package ru.yandex.practicum.kafka.telemetry.collector.handler;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.kafka.telemetry.collector.model.DeviceAction;
import ru.yandex.practicum.kafka.telemetry.collector.model.ScenarioAddedEvent;
import ru.yandex.practicum.kafka.telemetry.collector.model.ScenarioCondition;
import ru.yandex.practicum.kafka.telemetry.collector.model.enums.ActionType;
import ru.yandex.practicum.kafka.telemetry.collector.model.enums.ConditionOperation;
import ru.yandex.practicum.kafka.telemetry.collector.model.enums.ConditionType;
import ru.yandex.practicum.kafka.telemetry.collector.service.EventService;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioAddedEventHandler implements HubEventHandler {
    private final EventService hubEventService;

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_ADDED;
    }

    @Override
    public void handle(HubEventProto event) {
        log.info("Handling ScenarioAddedEvent: {}", event);
        ScenarioAddedEvent scenarioEvent = new ScenarioAddedEvent();
        scenarioEvent.setHubId(event.getHubId());
        scenarioEvent.setTimestamp(convertTimestamp(event.getTimestamp()));
        scenarioEvent.setName(event.getScenarioAdded().getName());
        scenarioEvent.setConditions(convertConditions(event.getScenarioAdded().getConditionList()));
        scenarioEvent.setActions(convertActions(event.getScenarioAdded().getActionList()));

        hubEventService.processEvent(scenarioEvent);
    }

    private List<ScenarioCondition> convertConditions(List<ScenarioConditionProto> protoConditions) {
        return protoConditions.stream()
                .map(proto -> {
                    ScenarioCondition condition = new ScenarioCondition();
                    condition.setSensorId(proto.getSensorId());
                    condition.setType(ConditionType.valueOf(proto.getType().name()));
                    condition.setOperation(ConditionOperation.valueOf(proto.getOperation().name()));
                    if (proto.getValueCase() == ScenarioConditionProto.ValueCase.BOOL_VALUE) {
                        condition.setValue(proto.getBoolValue());
                    } else if (proto.getValueCase() == ScenarioConditionProto.ValueCase.INT_VALUE) {
                        condition.setValue(proto.getIntValue());
                    }
                    return condition;
                })
                .collect(Collectors.toList());
    }

    private List<DeviceAction> convertActions(List<DeviceActionProto> protoActions) {
        return protoActions.stream()
                .map(proto -> {
                    DeviceAction action = new DeviceAction();
                    action.setSensorId(proto.getSensorId());
                    action.setType(ActionType.valueOf(proto.getType().name()));
                    if (proto.hasValue()) {
                        action.setValue(proto.getValue());
                    }
                    return action;
                })
                .collect(Collectors.toList());
    }

    private Instant convertTimestamp(Timestamp protoTimestamp) {
        return Instant.ofEpochSecond(protoTimestamp.getSeconds(), protoTimestamp.getNanos());
    }
}