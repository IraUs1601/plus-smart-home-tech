package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import ru.yandex.practicum.kafka.telemetry.collector.model.*;
import ru.yandex.practicum.kafka.telemetry.event.*;
import java.util.stream.Collectors;

public class HubEventMapper {

    public static HubEventAvro toAvro(HubEvent event) {
        var builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        switch (event) {
            case DeviceAddedEvent e -> builder.setPayload(
                    DeviceAddedEventAvro.newBuilder()
                            .setId(e.getId())
                            .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                            .build());
            case DeviceRemovedEvent e -> builder.setPayload(
                    DeviceRemovedEventAvro.newBuilder()
                            .setId(e.getId())
                            .build());
            case ScenarioAddedEvent e -> builder.setPayload(
                    ScenarioAddedEventAvro.newBuilder()
                            .setName(e.getName())
                            .setConditions(e.getConditions().stream()
                                    .map(c -> ScenarioConditionAvro.newBuilder()
                                            .setSensorId(c.getSensorId())
                                            .setType(ConditionTypeAvro.valueOf(c.getType().name()))
                                            .setOperation(ConditionOperationAvro.valueOf(c.getOperation().name()))
                                            .setValue(c.getValue())
                                            .build())
                                    .collect(Collectors.toList()))
                            .setActions(e.getActions().stream()
                                    .map(a -> DeviceActionAvro.newBuilder()
                                            .setSensorId(a.getSensorId())
                                            .setType(ActionTypeAvro.valueOf(a.getType().name()))
                                            .setValue(a.getValue())
                                            .build())
                                    .collect(Collectors.toList()))
                            .build());
            case ScenarioRemovedEvent e -> builder.setPayload(
                    ScenarioRemovedEventAvro.newBuilder()
                            .setName(e.getName())
                            .build());
            default -> throw new IllegalArgumentException("Unsupported hub event: " + event.getClass());
        }
        return builder.build();
    }
}