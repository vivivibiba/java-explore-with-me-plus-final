package ru.practicum.ewm.stats.collector.service;

import com.google.protobuf.Empty;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import ru.practicum.ewm.stats.avro.ActionTypeAvro;
import ru.practicum.ewm.stats.avro.UserActionAvro;
import ru.practicum.ewm.stats.grpc.collector.UserActionControllerGrpc;
import ru.practicum.ewm.stats.proto.ActionTypeProto;
import ru.practicum.ewm.stats.proto.UserActionProto;

import java.time.Instant;

@GrpcService
@RequiredArgsConstructor
public class UserActionGrpcService extends UserActionControllerGrpc.UserActionControllerImplBase {
    private final KafkaTemplate<Long, UserActionAvro> kafkaTemplate;

    @Value("${collector.kafka.user-actions-topic}")
    private String userActionsTopic;

    @Override
    public void collectUserAction(UserActionProto request, StreamObserver<Empty> responseObserver) {
        UserActionAvro action = new UserActionAvro();
        action.setUserId(request.getUserId());
        action.setEventId(request.getEventId());
        action.setActionType(toAvro(request.getActionType()));
        action.setTimestamp(Instant.ofEpochSecond(
                request.getTimestamp().getSeconds(),
                request.getTimestamp().getNanos()
        ));

        kafkaTemplate.send(userActionsTopic, request.getUserId(), action).join();
        responseObserver.onNext(Empty.getDefaultInstance());
        responseObserver.onCompleted();
    }

    private ActionTypeAvro toAvro(ActionTypeProto actionType) {
        return switch (actionType) {
            case ACTION_VIEW -> ActionTypeAvro.VIEW;
            case ACTION_REGISTER -> ActionTypeAvro.REGISTER;
            case ACTION_LIKE -> ActionTypeAvro.LIKE;
            case UNRECOGNIZED -> throw new IllegalArgumentException("Unknown user action type");
        };
    }
}
