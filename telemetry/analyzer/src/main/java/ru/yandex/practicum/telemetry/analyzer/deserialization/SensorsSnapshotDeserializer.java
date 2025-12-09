package ru.yandex.practicum.telemetry.analyzer.deserialization;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.io.IOException;
import java.util.Map;

@Slf4j
public class SensorsSnapshotDeserializer implements Deserializer<SensorsSnapshotAvro> {
    private final DecoderFactory decoderFactory = DecoderFactory.get();
    private final Schema schema = SensorsSnapshotAvro.getClassSchema();

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
    }

    @Override
    public SensorsSnapshotAvro deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            log.warn("Received null or empty data for topic: {}", topic);
            return null;
        }
        try {
            Decoder decoder = decoderFactory.binaryDecoder(data, null);
            DatumReader<SensorsSnapshotAvro> reader = new SpecificDatumReader<>(schema);
            SensorsSnapshotAvro result = reader.read(null, decoder);
            log.debug("Successfully deserialized SensorsSnapshotAvro from topic: {}", topic);
            return result;
        } catch (IOException e) {
            log.error("Error deserializing SensorsSnapshotAvro from topic {}: {}", topic, e.getMessage(), e);
            throw new RuntimeException("Failed to deserialize SensorsSnapshotAvro", e);
        }
    }

    @Override
    public void close() {
    }
}