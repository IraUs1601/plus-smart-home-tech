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
import ru.yandex.practicum.telemetry.analyzer.model.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.List;
import java.util.Objects;
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
        String hubId = event.getHubId();

        Scenario scenario = scenarioRepository.findByHubIdAndName(hubId, payload.getName())
                .orElseGet(() -> {
                    Scenario newScenario = Scenario.builder()
                            .hubId(hubId)
                            .name(payload.getName())
                            .build();
                    return scenarioRepository.save(newScenario);
                });

        actionRepository.deleteByScenario(scenario);
        conditionRepository.deleteByScenario(scenario);

        List<Action> actions = payload.getActions().stream()
                .map(actionAvro -> buildActionIfSensorExists(actionAvro, scenario, hubId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!actions.isEmpty()) {
            actionRepository.saveAll(actions);
        }
        log.info("Saved {} actions for scenario {} (hubId={})", actions.size(), scenario.getName(), hubId);

        List<Condition> conditions = payload.getConditions().stream()
                .map(condAvro -> buildConditionIfSensorExists(condAvro, scenario, hubId))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (!conditions.isEmpty()) {
            conditionRepository.saveAll(conditions);
        }
        log.info("Saved {} conditions for scenario {} (hubId={})", conditions.size(), scenario.getName(), hubId);
    }

    private Action buildActionIfSensorExists(DeviceActionAvro actionAvro, Scenario scenario, String hubId) {
        String sensorId = actionAvro.getSensorId();
        Sensor sensor = sensorRepository.findByIdAndHubId(sensorId, hubId)
                .orElse(null);

        if (sensor == null) {
            log.warn("Skipping action for scenario {}: sensor {} not found in hub {}",
                    scenario.getName(), sensorId, hubId);
            return null;
        }

        return Action.builder()
                .sensor(sensor)
                .scenario(scenario)
                .type(actionAvro.getType())
                .value(actionAvro.getValue() != null ? actionAvro.getValue() : 0)
                .build();
    }

    private Condition buildConditionIfSensorExists(ScenarioConditionAvro conditionAvro,
                                                   Scenario scenario,
                                                   String hubId) {
        String sensorId = conditionAvro.getSensorId();
        Sensor sensor = sensorRepository.findByIdAndHubId(sensorId, hubId)
                .orElse(null);

        if (sensor == null) {
            log.warn("Skipping condition for scenario {}: sensor {} not found in hub {}",
                    scenario.getName(), sensorId, hubId);
            return null;
        }

        Integer mappedValue = mapValue(conditionAvro.getValue());

        return Condition.builder()
                .sensor(sensor)
                .scenario(scenario)
                .type(conditionAvro.getType())
                .operation(conditionAvro.getOperation())
                .value(mappedValue)
                .build();
    }

    private Integer mapValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer i) {
            return i;
        }
        if (value instanceof Boolean b) {
            return b ? 1 : 0;
        }
        log.warn("Unsupported condition value type: {} ({})", value, value.getClass());
        return null;
    }

    @Override
    public String getEventType() {
        return ScenarioAddedEventAvro.class.getSimpleName();
    }
}