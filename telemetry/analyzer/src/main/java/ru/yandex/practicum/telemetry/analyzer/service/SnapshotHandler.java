package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;
import ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro;
import ru.yandex.practicum.telemetry.analyzer.client.HubRouterClient;
import ru.yandex.practicum.telemetry.analyzer.model.Action;
import ru.yandex.practicum.telemetry.analyzer.model.Condition;
import ru.yandex.practicum.telemetry.analyzer.model.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SnapshotHandler {

    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final HubRouterClient hubRouterClient;

    public void handle(SensorsSnapshotAvro snapshot) {
        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        if (states == null || states.isEmpty()) {
            return;
        }

        List<Scenario> scenarios = scenarioRepository.findByHubId(snapshot.getHubId());
        if (scenarios.isEmpty()) {
            return;
        }

        scenarios.stream()
                .filter(scenario -> checkConditions(scenario, states))
                .forEach(this::executeActions);
    }

    private boolean checkConditions(Scenario scenario, Map<String, SensorStateAvro> states) {
        List<Condition> conditions = conditionRepository.findAllByScenario(scenario);
        if (conditions.isEmpty()) {
            return false;
        }
        return conditions.stream().allMatch(condition -> evaluateCondition(scenario, condition, states));
    }

    private boolean evaluateCondition(Scenario scenario,
                                      Condition condition,
                                      Map<String, SensorStateAvro> states) {

        String sensorId = condition.getSensor().getId();
        SensorStateAvro state = states.get(sensorId);
        if (state == null) {
            return false;
        }

        Object data = state.getData();
        Integer sensorValue = extractSensorValue(data, condition.getType());
        if (sensorValue == null) {
            return false;
        }

        Integer expected = condition.getValue();
        return switch (condition.getOperation()) {
            case EQUALS -> sensorValue.equals(expected);
            case LOWER_THAN -> sensorValue < expected;
            case GREATER_THAN -> sensorValue > expected;
            default -> false;
        };
    }

    private Integer extractSensorValue(Object data, ConditionTypeAvro type) {
        if (data == null) {
            return null;
        }

        return switch (type) {
            case TEMPERATURE -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield climate.getTemperatureC();
                }
                yield null;
            }
            case HUMIDITY -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield climate.getHumidity();
                }
                yield null;
            }
            case CO2LEVEL -> {
                if (data instanceof ClimateSensorAvro climate) {
                    yield climate.getCo2Level();
                }
                yield null;
            }
            case LUMINOSITY -> {
                if (data instanceof LightSensorAvro light) {
                    yield light.getLuminosity();
                }
                yield null;
            }
            case MOTION -> {
                if (data instanceof MotionSensorAvro motion) {
                    yield motion.getMotion() ? 1 : 0;
                }
                yield null;
            }
            case SWITCH -> {
                if (data instanceof SwitchSensorAvro switchSensor) {
                    yield switchSensor.getState() ? 1 : 0;
                }
                yield null;
            }
        };
    }

    private void executeActions(Scenario scenario) {
        List<Action> actions = actionRepository.findAllByScenario(scenario);
        if (actions.isEmpty()) {
            return;
        }
        actions.forEach(hubRouterClient::sendAction);
    }
}