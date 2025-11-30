package ru.yandex.practicum.kafka.telemetry.collector.mapper;

import ru.yandex.practicum.kafka.telemetry.collector.model.*;
import ru.yandex.practicum.kafka.telemetry.event.*;

public class SensorEventMapper {

    public static SensorEventAvro toAvro(SensorEvent event) {
        var builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        switch (event) {
            case ClimateSensorEvent e -> builder.setPayload(
                    ClimateSensorAvro.newBuilder()
                            .setTemperatureC(e.getTemperatureC())
                            .setHumidity(e.getHumidity())
                            .setCo2Level(e.getCo2Level())
                            .build());
            case LightSensorEvent e -> builder.setPayload(
                    LightSensorAvro.newBuilder()
                            .setLinkQuality(e.getLinkQuality())
                            .setLuminosity(e.getLuminosity())
                            .build());
            case MotionSensorEvent e -> builder.setPayload(
                    MotionSensorAvro.newBuilder()
                            .setLinkQuality(e.getLinkQuality())
                            .setMotion(e.getMotion())
                            .setVoltage(e.getVoltage())
                            .build());
            case SwitchSensorEvent e -> builder.setPayload(
                    SwitchSensorAvro.newBuilder()
                            .setState(e.getState())
                            .build());
            case TemperatureSensorEvent e -> builder.setPayload(
                    TemperatureSensorAvro.newBuilder()
                            .setTemperatureC(e.getTemperatureC())
                            .setTemperatureF(e.getTemperatureF())
                            .build());
            default -> throw new IllegalArgumentException("Unsupported sensor event: " + event.getClass());
        }
        return builder.build();
    }
}