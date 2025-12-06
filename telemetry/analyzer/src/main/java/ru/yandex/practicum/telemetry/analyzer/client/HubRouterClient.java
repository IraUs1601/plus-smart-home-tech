package ru.yandex.practicum.telemetry.analyzer.client;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
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
        log.info("Initialized gRPC client for address: {}", address);
    }

    public void sendAction(Action action) {
        try {
            if (action == null || action.getScenario() == null || action.getSensor() == null) {
                log.error("Invalid action: action={}, scenario={}, sensor={}", action,
                        action != null ? action.getScenario() : null,
                        action != null ? action.getSensor() : null);
                return;
            }
            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(action.getScenario().getHubId())
                    .setScenarioName(action.getScenario().getName())
                    .setAction(DeviceActionProto.newBuilder()
                            .setSensorId(action.getSensor().getId())
                            .setType(mapActionType(action.getType()))
                            .setValue(action.getValue() != null ? action.getValue() : 0)
                            .build())
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano())
                            .build())
                    .build();

            stub.handleDeviceAction(request);
            log.info("Sent action {} for device {} in scenario {} for hub {}",
                    request.getAction().getType(), action.getSensor().getId(),
                    action.getScenario().getName(), action.getScenario().getHubId());
        } catch (Exception e) {
            log.error("Failed to send action for device {}: {}",
                    action.getSensor().getId(), e.getMessage(), e);
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
                log.info("gRPC channel shutdown");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Interrupted during gRPC channel shutdown: {}", e.getMessage(), e);
            }
        }
    }
}