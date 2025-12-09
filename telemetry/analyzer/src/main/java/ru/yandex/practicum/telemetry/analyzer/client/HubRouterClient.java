package ru.yandex.practicum.telemetry.analyzer.client;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.telemetry.analyzer.model.Action;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class HubRouterClient {

    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;
    private final ManagedChannel channel;

    public HubRouterClient(@Value("${grpc.client.hub-router.address}") String grpcAddress) {
        String address = grpcAddress.replace("static://", "");

        this.channel = ManagedChannelBuilder.forTarget(address)
                .usePlaintext()
                .keepAliveTime(30, TimeUnit.SECONDS)
                .keepAliveWithoutCalls(true)
                .build();

        this.stub = HubRouterControllerGrpc.newBlockingStub(channel);
    }

    public void sendAction(Action action) {
        if (action == null || action.getScenario() == null || action.getSensor() == null) {
            return;
        }

        try {
            String hubId = action.getScenario().getHubId();
            String scenarioName = action.getScenario().getName();
            String sensorId = action.getSensor().getId();

            int value = action.getValue() != null ? action.getValue() : 0;

            DeviceActionProto deviceAction = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(mapActionType(action.getType()))
                    .setValue(value)
                    .build();

            Instant now = Instant.now();

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(hubId)
                    .setScenarioName(scenarioName)
                    .setAction(deviceAction)
                    .setTimestamp(
                            Timestamp.newBuilder()
                                    .setSeconds(now.getEpochSecond())
                                    .setNanos(now.getNano())
                                    .build()
                    )
                    .build();

            stub.handleDeviceAction(request);

        } catch (StatusRuntimeException e) {
            log.warn("Failed to send gRPC action: {}", e.getStatus());
        } catch (Exception e) {
            log.warn("Error while sending gRPC action: {}", e.getMessage());
        }
    }

    private ActionTypeProto mapActionType(ActionTypeAvro type) {
        return switch (type) {
            case ACTIVATE -> ActionTypeProto.ACTIVATE;
            case DEACTIVATE -> ActionTypeProto.DEACTIVATE;
            case INVERSE -> ActionTypeProto.INVERSE;
            case SET_VALUE -> ActionTypeProto.SET_VALUE;
        };
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null && !channel.isShutdown()) {
            try {
                channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}