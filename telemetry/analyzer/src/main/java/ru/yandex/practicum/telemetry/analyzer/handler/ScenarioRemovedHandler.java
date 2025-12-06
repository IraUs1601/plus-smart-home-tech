package ru.yandex.practicum.telemetry.analyzer.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScenarioRemovedHandler implements HubEventHandler {
    private final ScenarioRepository scenarioRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;

    @Override
    public void handle(HubEventAvro event) {
        ScenarioRemovedEventAvro payload = (ScenarioRemovedEventAvro) event.getPayload();
        scenarioRepository.findByHubIdAndName(event.getHubId(), payload.getName())
                .ifPresent(scenario -> {
                    actionRepository.deleteByScenario(scenario);
                    conditionRepository.deleteByScenario(scenario);
                    scenarioRepository.delete(scenario);
                    log.info("Removed scenario {} for hub {}", payload.getName(), event.getHubId());
                });
    }

    @Override
    public String getEventType() {
        return ScenarioRemovedEventAvro.class.getSimpleName();
    }
}