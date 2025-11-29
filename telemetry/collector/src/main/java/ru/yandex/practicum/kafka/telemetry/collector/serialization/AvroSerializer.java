package ru.yandex.practicum.kafka.telemetry.collector.serialization;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class AvroSerializer implements Serializer<SpecificRecordBase> {

    private final EncoderFactory encoderFactory = EncoderFactory.get();

    @Override
    public byte[] serialize(String topic, SpecificRecordBase data) {
        if (data == null) {
            return null;
        }
        String payloadType = "unknown";
        if (data instanceof ru.yandex.practicum.kafka.telemetry.event.HubEventAvro) {
            payloadType = ((ru.yandex.practicum.kafka.telemetry.event.HubEventAvro) data).getPayload().getClass().getSimpleName();
        } else if (data instanceof ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro) {
            payloadType = ((ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro) data).getPayload().getClass().getSimpleName();
        }
        log.info("Serializing Avro data: type={}, topic={}, payloadType={}",
                data.getClass().getSimpleName(), topic, payloadType);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = encoderFactory.binaryEncoder(out, null);
            DatumWriter<SpecificRecordBase> writer = new SpecificDatumWriter<>(data.getSchema());
            writer.write(data, encoder);
            encoder.flush();
            return out.toByteArray();
        } catch (IOException e) {
            log.error("Failed to serialize Avro data: type={}, topic={}, payloadType={}",
                    data.getClass().getSimpleName(), topic, payloadType, e);
            throw new SerializationException("Failed to serialize Avro data for topic [" + topic + "]", e);
        }
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public void close() {
    }
}