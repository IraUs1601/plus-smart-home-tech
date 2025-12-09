package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ScenarioAddedHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;

    @Override
    public void handle(HubEventAvro event) {
        ScenarioAddedEventAvro payload = (ScenarioAddedEventAvro) event.getPayload();
        if (payload == null) {
            return;
        }

        Scenario scenario = scenarioRepository.findByHubIdAndName(event.getHubId(), payload.getName())
                .orElseGet(() -> scenarioRepository.save(
                        Scenario.builder()
                                .hubId(event.getHubId())
                                .name(payload.getName())
                                .build()
                ));
        List<String> actionSensorIds = payload.getActions().stream()
                .map(DeviceActionAvro::getSensorId)
                .toList();

        if (sensorRepository.existsByIdInAndHubId(actionSensorIds, event.getHubId())) {

            List<Action> actions = payload.getActions().stream()
                    .map(a -> Action.builder()
                            .sensor(sensorRepository.findById(a.getSensorId()).orElseThrow())
                            .scenario(scenario)
                            .type(a.getType())
                            .value(a.getValue() != null ? a.getValue() : 0)
                            .build()
                    ).collect(Collectors.toList());

            actionRepository.saveAll(actions);
        }

        List<String> conditionSensorIds = payload.getConditions().stream()
                .map(ScenarioConditionAvro::getSensorId)
                .toList();
        if (sensorRepository.existsByIdInAndHubId(conditionSensorIds, event.getHubId())) {
            List<Condition> conditions = payload.getConditions().stream()
                    .map(c -> Condition.builder()
                            .sensor(sensorRepository.findById(c.getSensorId()).orElseThrow())
                            .scenario(scenario)
                            .type(c.getType())
                            .operation(c.getOperation())
                            .value(mapValue(c.getValue()))
                            .build()
                    ).collect(Collectors.toList());

            conditionRepository.saveAll(conditions);
        }
    }

    private Integer mapValue(Object value) {
        if (value instanceof Integer i) {
            return i;
        }
        return (Boolean) value ? 1 : 0;
    }

    @Override
    public String getEventType() {
        return ScenarioAddedEventAvro.class.getSimpleName();
    }
}