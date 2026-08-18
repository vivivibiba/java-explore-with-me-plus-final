package ru.practicum.explorewithme;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.practicum.ewm.stats.grpc.dashboard.RecommendationsControllerGrpc;
import ru.practicum.ewm.stats.proto.InteractionsCountRequestProto;
import ru.practicum.ewm.stats.proto.RecommendedEventProto;
import ru.practicum.ewm.stats.proto.SimilarEventsRequestProto;
import ru.practicum.ewm.stats.proto.UserPredictionsRequestProto;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

@Component
public class AnalyzerClient {
    @GrpcClient("analyzer")
    private RecommendationsControllerGrpc.RecommendationsControllerBlockingStub client;

    public Stream<RecommendedEvent> getRecommendationsForUser(long userId, int maxResults) {
        UserPredictionsRequestProto request = UserPredictionsRequestProto.newBuilder()
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return map(client.getRecommendationsForUser(request));
    }

    public Stream<RecommendedEvent> getSimilarEvents(long eventId, long userId, int maxResults) {
        SimilarEventsRequestProto request = SimilarEventsRequestProto.newBuilder()
                .setEventId(eventId)
                .setUserId(userId)
                .setMaxResults(maxResults)
                .build();
        return map(client.getSimilarEvents(request));
    }

    public Stream<RecommendedEvent> getInteractionsCount(List<Long> eventIds) {
        InteractionsCountRequestProto request = InteractionsCountRequestProto.newBuilder()
                .addAllEventId(eventIds)
                .build();
        return map(client.getInteractionsCount(request));
    }

    private Stream<RecommendedEvent> map(Iterator<RecommendedEventProto> iterator) {
        return StreamSupport.stream(
                        Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),
                        false
                )
                .map(value -> new RecommendedEvent(value.getEventId(), value.getScore()));
    }
}
