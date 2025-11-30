package ru.yandex.practicum.kafka.telemetry.collector.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.yandex.practicum.kafka.telemetry.collector.model.enums.SensorEventType;

@Getter
@Setter
@ToString(callSuper = true)
@FieldDefaults(level = lombok.AccessLevel.PRIVATE)
public class LightSensorEvent extends SensorEvent {
    Integer linkQuality;
    Integer luminosity;

    @Override
    public SensorEventType getType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}