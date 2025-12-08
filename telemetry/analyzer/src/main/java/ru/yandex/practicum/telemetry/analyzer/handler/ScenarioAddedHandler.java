package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        Scenario scenario = scenarioRepository.findByHubIdAndName(event.getHubId(), payload.getName())
                .orElseGet(() -> {
                    Scenario newScenario = Scenario.builder()
                            .hubId(event.getHubId())
                            .name(payload.getName())
                            .build();
                    return scenarioRepository.save(newScenario);
                });

        List<String> actionSensorIds = payload.getActions().stream()
                .map(DeviceActionAvro::getSensorId)
                .toList();
        if (sensorRepository.existsByIdInAndHubId(actionSensorIds, event.getHubId())) {
            List<Action> actions = payload.getActions().stream()
                    .map(action -> Action.builder()
                            .sensor(sensorRepository.findById(action.getSensorId()).orElseThrow())
                            .scenario(scenario)
                            .type(action.getType())
                            .value(action.getValue() != null ? action.getValue() : 0)
                            .build())
                    .collect(Collectors.toList());
            actionRepository.saveAll(actions);
            log.info("Saved {} actions for scenario {}", actions.size(), scenario.getName());
        }

        List<String> conditionSensorIds = payload.getConditions().stream()
                .map(ScenarioConditionAvro::getSensorId)
                .toList();
        if (sensorRepository.existsByIdInAndHubId(conditionSensorIds, event.getHubId())) {
            List<Condition> conditions = payload.getConditions().stream()
                    .map(condition -> Condition.builder()
                            .sensor(sensorRepository.findById(condition.getSensorId()).orElseThrow())
                            .scenario(scenario)
                            .type(condition.getType())
                            .operation(condition.getOperation())
                            .value(mapValue(condition.getValue()))
                            .build())
                    .collect(Collectors.toList());
            conditionRepository.saveAll(conditions);
            log.info("Saved {} conditions for scenario {}", conditions.size(), scenario.getName());
        }
    }

    private Integer mapValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        }
        return (Boolean) value ? 1 : 0;
    }

    @Override
    public String getEventType() {
        return ScenarioAddedEventAvro.class.getSimpleName();
    }
}