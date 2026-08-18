package ru.practicum.ewm.stats.analyzer.service;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.practicum.ewm.stats.grpc.dashboard.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.List;

@GrpcService
@RequiredArgsConstructor
public class RecommendationsGrpcService extends RecommendationsControllerGrpc.RecommendationsControllerImplBase {
    private final RecommendationService recommendationService;

    @Override
    public void getRecommendationsForUser(UserPredictionsRequestProto request,
                                          StreamObserver<RecommendedEventProto> responseObserver) {
        send(
                recommendationService.getRecommendationsForUser(request.getUserId(), request.getMaxResults()),
                responseObserver
        );
    }

    @Override
    public void getSimilarEvents(SimilarEventsRequestProto request,
                                 StreamObserver<RecommendedEventProto> responseObserver) {
        send(
                recommendationService.getSimilarEvents(
                        request.getEventId(),
                        request.getUserId(),
                        request.getMaxResults()
                ),
                responseObserver
        );
    }

    @Override
    public void getInteractionsCount(InteractionsCountRequestProto request,
                                     StreamObserver<RecommendedEventProto> responseObserver) {
        send(recommendationService.getInteractionsCount(request.getEventIdList()), responseObserver);
    }

    private void send(List<Recommendation> recommendations,
                      StreamObserver<RecommendedEventProto> responseObserver) {
        try {
            for (Recommendation recommendation : recommendations) {
                responseObserver.onNext(RecommendedEventProto.newBuilder()
                        .setEventId(recommendation.eventId())
                        .setScore(recommendation.score())
                        .build());
            }
            responseObserver.onCompleted();
        } catch (RuntimeException exception) {
            responseObserver.onError(
                    Status.INTERNAL.withDescription(exception.getMessage()).withCause(exception).asRuntimeException()
            );
        }
    }
}
