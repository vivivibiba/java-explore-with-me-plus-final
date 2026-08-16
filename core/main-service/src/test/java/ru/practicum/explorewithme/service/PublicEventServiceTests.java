package ru.practicum.explorewithme.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.dto.event.EventDto;
import ru.practicum.explorewithme.dto.event.EventShortDto;
import ru.practicum.explorewithme.dto.event.EventStatus;
import ru.practicum.explorewithme.dto.event.PublicEventSort;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.hit.EndpointHitRequest;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PublicEventServiceTests {
    private static final long USER_ID = 1L;
    private static final long CATEGORY_ID = 1L;
    private static final String IP = "127.0.0.1";
    private static final String EVENTS_URI = "/events";

    @InjectMocks
    private EventServiceImpl eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @Mock
    private RequestService requestService;

    @Mock
    private StatsClient statsClient;

    @Test
    void getPublishedEvents_returnsEventsWithViewsFromStatsMap() {
        Event first = createEvent(1L, "First event", EventStatus.PUBLISHED, 0);
        Event second = createEvent(2L, "Second event", EventStatus.PUBLISHED, 0);
        List<Event> events = List.of(first, second);

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(events));
        when(statsClient.getStatistics(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(
                        new ViewStatsResponse("ewm-main-service", "/events/1", 11L),
                        new ViewStatsResponse("ewm-main-service", "/events/2", 22L)
                ));

        List<EventShortDto> result = eventService.getPublishedEvents(
                null,
                null,
                null,
                null,
                null,
                false,
                PublicEventSort.EVENT_DATE,
                0,
                10,
                IP,
                EVENTS_URI
        );

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(0).getViews()).isEqualTo(11L);
        assertThat(result.get(1).getId()).isEqualTo(2L);
        assertThat(result.get(1).getViews()).isEqualTo(22L);
        verify(statsClient).addStatistics(any(EndpointHitRequest.class));
        verify(statsClient).getStatistics(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true));
    }

    @Test
    void getPublishedEvents_doesNotRequestStatsWhenEventsNotFound() {
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        List<EventShortDto> result = eventService.getPublishedEvents(
                null,
                null,
                null,
                null,
                null,
                false,
                PublicEventSort.EVENT_DATE,
                0,
                10,
                IP,
                EVENTS_URI
        );

        assertThat(result).isEmpty();
        verify(statsClient).addStatistics(any(EndpointHitRequest.class));
        verify(statsClient, never()).getStatistics(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true));
    }

    @Test
    void getPublishedEvent_returnsEventWithViewsFromStatsMap() {
        Event event = createEvent(3L, "Published event", EventStatus.PUBLISHED, 0);
        when(eventRepository.findByIdAndStatus(3L, EventStatus.PUBLISHED)).thenReturn(Optional.of(event));
        when(statsClient.getStatistics(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(new ViewStatsResponse("ewm-main-service", "/events/3", 33L)));

        EventDto result = eventService.getPublishedEvent(3L, IP, "/events/3");

        assertThat(result.getId()).isEqualTo(3L);
        assertThat(result.getViews()).isEqualTo(33L);
        assertThat(result.getCategory().getId()).isEqualTo(CATEGORY_ID);
        verify(statsClient).addStatistics(any(EndpointHitRequest.class));
    }

    private Event createEvent(long id, String title, EventStatus status, int confirmedRequests) {
        return Event.builder()
                .id(id)
                .annotation("Annotation with enough length")
                .category(Category.builder().id(CATEGORY_ID).name("Category").build())
                .confirmedRequests(confirmedRequests)
                .createdOn(LocalDateTime.now().minusDays(1))
                .description("Description with enough length")
                .eventDate(LocalDateTime.now().plusDays(id))
                .initiator(User.builder().id(USER_ID).name("User").email("user@example.com").build())
                .location(new LocationEmbeddable(55.0, 37.0))
                .paid(false)
                .participantLimit(10)
                .publishedOn(LocalDateTime.now().minusHours(1))
                .requestModeration(true)
                .status(status)
                .title(title)
                .build();
    }
}
