package ru.practicum.explorewithme.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.practicum.explorewithme.StatsClient;
import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.request.ChangedRequestStatusesDto;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;
import ru.practicum.explorewithme.dto.request.UpdateRequestStatusDto;
import ru.practicum.explorewithme.entity.Category;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.LocationEmbeddable;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.repository.CategoryRepository;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.specification.AdminEventSearchSpecification;
import ru.practicum.explorewithme.repository.UserRepository;
import ru.practicum.explorewithme.stats.ViewStatsResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class PrivateEventServiceTests {

    @InjectMocks
    private EventServiceImpl eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserService userService;

    @Mock
    private RequestService requestService;

    @Mock
    private StatsClient statsClient;

    @Mock
    private AdminEventSearchSpecification spec;

    private static final long USER_ID = 1L;
    private static final long EVENT_ID = 1L;
    private static final long CATEGORY_ID = 1L;

    private NewEventDto newEventDto;
    private UserUpdateEventDto userUpdateEventDto;
    private UpdateRequestStatusDto updateRequestStatusDto;

    @BeforeEach
    void setUp() {
        Location location = new Location(1.0, 1.0);
        newEventDto = NewEventDto.builder()
                .title("Event Title")
                .description("Description")
                .annotation("Annotation")
                .category(1L)
                .eventDate(LocalDateTime.now().plusHours(3))
                .location(location)
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .build();

        userUpdateEventDto = UserUpdateEventDto.builder()
                .title("Updated Title")
                .status(UserEventUpdateAction.SEND_TO_REVIEW)
                .build();

        updateRequestStatusDto = UpdateRequestStatusDto.builder()
                .status(RequestStatus.CONFIRMED)
                .requestIds(List.of(1L, 2L))
                .build();
    }


    @Test
    void getEvents_returnsListWithStats() {
        int from = 0;
        int size = 5;

        Event event = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PENDING);

        List<Event> events = List.of(event);

        String targetUri = "/events/" + event.getId();

        ViewStatsResponse stats = new ViewStatsResponse(null, targetUri, 7L);
        List<ViewStatsResponse> statsList = List.of(stats);

        when(eventRepository.findByInitiatorId(eq(USER_ID), any(Pageable.class)))
                .thenReturn(events);

        when(statsClient.getStatistics(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList(),
                eq(true)))
                .thenReturn(statsList);

        List<EventDto> result = eventService.getEvents(USER_ID, from, size);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(event.getId());
        assertThat(result.get(0).getViews()).isEqualTo(7L);

        verify(eventRepository).findByInitiatorId(eq(USER_ID), any(Pageable.class));
        verify(statsClient).getStatistics(any(), any(), any(), eq(true));
    }

    @Test
    void createEvent_createsEventWithCorrectFields() {
        NewEventDto request = createTestNewEventDto(CATEGORY_ID, LocalDateTime.now().plusHours(3));

        Event expectedEvent = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PENDING);
        Category category = createTestCategory(CATEGORY_ID);
        User initiator = createTestUser(USER_ID);

        when(categoryRepository.findById(newEventDto.getCategory())).thenReturn(Optional.of(category));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(initiator));
        when(eventRepository.save(any(Event.class))).thenReturn(expectedEvent);

        EventDto result = eventService.createEvent(USER_ID, newEventDto);

        assertThat(result.getId()).isEqualTo(expectedEvent.getId());
        assertThat(result.getStatus()).isEqualTo(EventStatus.PENDING);

        verify(categoryRepository).findById(newEventDto.getCategory());
        verify(userRepository).findById(USER_ID);
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getEvent_returnsEventDtoWithStats() {
        Event event = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PENDING);

        String uri = "/events/" + EVENT_ID;

        ViewStatsResponse stats = new ViewStatsResponse(null, uri, 5L);

        when(eventRepository.findByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(Optional.of(event));
        when(statsClient.getStatistics(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(stats));

        EventDto result = eventService.getEvent(USER_ID, EVENT_ID);

        assertThat(result.getId()).isEqualTo(EVENT_ID);
        assertThat(result.getViews()).isEqualTo(5L);

        verify(eventRepository).findByIdAndInitiatorId(EVENT_ID, USER_ID);
        verify(statsClient).getStatistics(any(), any(), anyList(), eq(true));
    }

    @Test
    void getEvent_throwsNotFoundException_whenEventDoesNotBelongToUser() {
        when(eventRepository.findByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> eventService.getEvent(USER_ID, EVENT_ID));
    }

    @Test
    void updateEvent_updatesEventFields_whenStatusAllows() {
        Event existingEvent = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PENDING);

        UserUpdateEventDto update = createTestUpdateEventDto(UserEventUpdateAction.SEND_TO_REVIEW);

        when(eventRepository.findByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(existingEvent);

        String uri = "/events/" + EVENT_ID;
        ViewStatsResponse stats = new ViewStatsResponse(null, uri, 3L);
        when(statsClient.getStatistics(any(LocalDateTime.class), any(LocalDateTime.class), anyList(), eq(true)))
                .thenReturn(List.of(stats));

        EventDto result = eventService.updateEvent(USER_ID, EVENT_ID, update);

        assertThat(result.getTitle()).isEqualTo("Updated Event Title");
        assertThat(result.getDescription()).isEqualTo("Updated detailed description that satisfies the 20–7000 character size constraint.");
        assertThat(result.isPaid()).isEqualTo(true);

        verify(eventRepository).save(existingEvent);
    }

    @Test
    void updateEvent_throwsUnavailableUpdateException_whenStatusDoesNotAllowUpdate() {
        Event existingEvent = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PUBLISHED);

        UserUpdateEventDto update = UserUpdateEventDto.builder().build();

        when(eventRepository.findByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(Optional.of(existingEvent));

        assertThrows(UnavailableUpdateException.class,
                () -> eventService.updateEvent(USER_ID, EVENT_ID, update));
    }

    @Test
    void updateEvent_cancelsEvent_whenStatusActionIsCancel() {
        Event existingEvent = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PENDING);

        UserUpdateEventDto update = createTestUpdateEventDto(UserEventUpdateAction.CANCEL_REVIEW);

        when(eventRepository.findByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(existingEvent)).thenReturn(existingEvent);

        String uri = "/events/" + EVENT_ID;
        ViewStatsResponse stats = new ViewStatsResponse(null, uri, 0L);
        when(statsClient.getStatistics(any(), any(), anyList(), eq(true))).thenReturn(List.of(stats));

        EventDto result = eventService.updateEvent(USER_ID, EVENT_ID, update);

        assertThat(existingEvent.getStatus()).isEqualTo(EventStatus.CANCELED);
        assertThat(result.getStatus()).isEqualTo(EventStatus.CANCELED);
    }

    @Test
    void getRequests_returnsRequestsFromRequestService() {
        long requestId = 10L;
        RequestDto requestDto = createTestRequestDto(requestId, EVENT_ID, USER_ID, RequestStatus.PENDING);
        List<RequestDto> requests = List.of(requestDto);

        when(eventRepository.existsByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(true);
        when(requestService.getRequestsToUsersEvent(EVENT_ID)).thenReturn(requests);

        List<RequestDto> result = eventService.getRequests(USER_ID, EVENT_ID);

        assertThat(result).isEqualTo(requests);
        verify(requestService).getRequestsToUsersEvent(EVENT_ID);
    }

    @Test
    void getRequests_throwsNotFoundException_whenEventDoesNotExist() {
        when(eventRepository.existsByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> eventService.getRequests(USER_ID, EVENT_ID));
    }

    @Test
    void updateRequestStatuses_confirmsSomeAndRejectsOthers_whenLimitReached() {
        long requestId1 = 1L;
        long requestId2 = 2L;
        long requestId3 = 3L;
        List<Long> ids = List.of(requestId1, requestId2, requestId3);
        Event event = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PUBLISHED);
        event.setParticipantLimit(3);
        event.setConfirmedRequests(2);

        RequestDto req1 = createTestRequestDto(requestId1, EVENT_ID, 1, RequestStatus.PENDING);
        RequestDto req2 = createTestRequestDto(requestId2, EVENT_ID, 2, RequestStatus.PENDING);
        RequestDto req3 = createTestRequestDto(requestId3, EVENT_ID, 3, RequestStatus.PENDING);

        UpdateRequestStatusDto update = createTestUpdateRequestStatusDto(ids, RequestStatus.CONFIRMED);

        when(eventRepository.findByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(Optional.of(event));

        RequestDto confirmed = createTestRequestDto(requestId1, EVENT_ID, 1, RequestStatus.CONFIRMED);;
        RequestDto rejected1 = createTestRequestDto(requestId2, EVENT_ID, 2, RequestStatus.REJECTED);
        RequestDto rejected2 = createTestRequestDto(requestId3, EVENT_ID, 3, RequestStatus.REJECTED);

        when(requestService.changeRequestStatuses(List.of(1L), RequestStatus.CONFIRMED))
                .thenReturn(List.of(confirmed));
        when(requestService.changeRequestStatuses(List.of(2L, 3L), RequestStatus.REJECTED))
                .thenReturn(List.of(rejected1, rejected2));

        String uri = "/events/" + EVENT_ID;

        ChangedRequestStatusesDto result = eventService.updateRequestStatuses(USER_ID, EVENT_ID, update);

        assertThat(result.getConfirmedRequests()).hasSize(1).contains(confirmed);
        assertThat(result.getRejectedRequests()).hasSize(2).contains(rejected1, rejected2);
        assertThat(event.getConfirmedRequests()).isEqualTo(3);

        verify(eventRepository).save(event);
    }

    @Test
    void updateRequestStatuses_rejectsAll_whenNoSpotsAvailable() {
        long requestId1 = 1L;
        long requestId2 = 2L;
        long requestId3 = 3L;
        List<Long> ids = List.of(requestId1, requestId2, requestId3);
        Event event = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PUBLISHED);
        event.setParticipantLimit(2);
        event.setConfirmedRequests(2);

        UpdateRequestStatusDto update = createTestUpdateRequestStatusDto(ids, RequestStatus.CONFIRMED);

        when(eventRepository.findByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(Optional.of(event));

        assertThrows(UnavailableUpdateException.class,
                () -> eventService.updateRequestStatuses(USER_ID, EVENT_ID, update));
    }

    @Test
    void updateRequestStatuses_allowsAll_whenNoLimitOrNoModeration() {
        long requestId1 = 1L;
        long requestId2 = 2L;
        long requestId3 = 3L;
        List<Long> ids = List.of(requestId1, requestId2, requestId3);
        Event event = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PUBLISHED);
        event.setParticipantLimit(0);
        event.setRequestModeration(false);

        UpdateRequestStatusDto update = createTestUpdateRequestStatusDto(ids, RequestStatus.CONFIRMED);

        RequestDto confirmed1 = createTestRequestDto(requestId1, EVENT_ID, 1, RequestStatus.CONFIRMED);;
        RequestDto confirmed2 = createTestRequestDto(requestId2, EVENT_ID, 2, RequestStatus.CONFIRMED);
        RequestDto confirmed3 = createTestRequestDto(requestId3, EVENT_ID, 3, RequestStatus.CONFIRMED);

        when(eventRepository.findByIdAndInitiatorId(EVENT_ID, USER_ID)).thenReturn(Optional.of(event));
        when(requestService.changeRequestStatuses(ids, RequestStatus.CONFIRMED)).thenReturn(List.of(confirmed1, confirmed2, confirmed3));

        String uri = "/events/" + EVENT_ID;

        ChangedRequestStatusesDto result = eventService.updateRequestStatuses(USER_ID, EVENT_ID, update);

        assertThat(result.getConfirmedRequests()).containsExactlyInAnyOrder(confirmed1, confirmed2, confirmed3);
        assertThat(result.getRejectedRequests()).isEmpty();

        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void searchEvents_filtersAndPagination_returnsCorrectPage() {
        // Arrange
        List<Long> users = List.of(1L, 2L);
        List<String> states = List.of("PUBLISHED", "CANCELLED");
        List<Long> categories = List.of(3L);
        String rangeStart = "2024-01-01 00:00:00";
        String rangeEnd = "2024-12-31 23:59:59";
        int from = 5;
        int size = 10;

        Event e1 = createTestEvent(1L, 1L, 3L, EventStatus.PUBLISHED);
        Event e2 = createTestEvent(2L, 2L, 3L, EventStatus.CANCELED);
        List<Event> pageContent = List.of(e1, e2);

        Page<Event> page = new PageImpl<>(pageContent, PageRequest.of(0, size), 25L);

        when(eventRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(page);

        ViewStatsResponse s1 = new ViewStatsResponse(null, "/events/1", 10L);
        ViewStatsResponse s2 = new ViewStatsResponse(null, "/events/2", 20L);
        List<ViewStatsResponse> stats = List.of(s1, s2);

        when(statsClient.getStatistics(any(), any(), anyList(), anyBoolean()))
                .thenReturn(stats);

        List<EventDto> result = eventService.searchEvents(users, states, categories, rangeStart, rangeEnd, from, size);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getId()).isEqualTo(1L);
        assertThat(result.get(1).getId()).isEqualTo(2L);

        assertThat(result.get(0).getViews()).isEqualTo(10L);
        assertThat(result.get(1).getViews()).isEqualTo(20L);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(eventRepository).findAll(any(Specification.class), pageableCaptor.capture());

        Pageable captured = pageableCaptor.getValue();
        assertThat(captured.getOffset()).isEqualTo(5);
        assertThat(captured.getPageSize()).isEqualTo(10);
    }

    @Test
    void searchEvents_emptyResult_returnsEmptyList() {
        List<Long> users = Collections.emptyList();
        List<String> states = Collections.emptyList();
        List<Long> categories = Collections.emptyList();
        String rangeStart = null;
        String rangeEnd = null;
        int from = 0;
        int size = 5;

        Page<Event> emptyPage = new PageImpl<>(Collections.emptyList());
        when(eventRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(emptyPage);

        List<EventDto> result = eventService.searchEvents(users, states, categories, rangeStart, rangeEnd, from, size);

        assertThat(result).isEmpty();
    }

    @Test
    void updateEventByAdmin_validPendingEvent_successfullyUpdatesAndReturnsDto() {
        long eventId = 1L;
        long initiatorId = 2L;

        Event event = createTestEvent(eventId, initiatorId, 3L, EventStatus.PENDING);
        event.setParticipantLimit(10);
        event.setConfirmedRequests(2);

        AdminUpdateEventDto body = createUpdateEventDto(AdminEventUpdateAction.PUBLISH_EVENT);

        ViewStatsResponse stats = new ViewStatsResponse(null, "/events/" + eventId, 42L);
        when(statsClient.getStatistics(any(), any(), anyList(), anyBoolean())).thenReturn(List.of(stats));

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventDto result = eventService.updateEvent(eventId, body);

        assertThat(result.getId()).isEqualTo(eventId);
        assertThat(result.getStatus()).isEqualTo(EventStatus.PUBLISHED);
        assertThat(result.getViews()).isEqualTo(42L);

        verify(eventRepository).save(event);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    void updateEventByAdmin_eventNotFound_throwsNotFoundException() {
        long eventId = 999L;
        AdminUpdateEventDto body = createUpdateEventDto(AdminEventUpdateAction.PUBLISH_EVENT);

        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.updateEvent(eventId, body))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void updateEventByAdmin_nonPendingEvent_throwsUnavailableUpdateException() {
        Event event = createTestEvent(EVENT_ID, USER_ID, CATEGORY_ID, EventStatus.PUBLISHED);

        AdminUpdateEventDto body = createUpdateEventDto(AdminEventUpdateAction.REJECT_EVENT);

        when(eventRepository.findById(EVENT_ID)).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.updateEvent(EVENT_ID, body))
                .isInstanceOf(UnavailableUpdateException.class)
                .hasMessageContaining(Entities.EVENT.name());

        verify(eventRepository, never()).save(any());
    }

    @Test
    void updateEvent_rejectAction_mapsToCancelledStatus() {
        long eventId = 1L;
        Event event = createTestEvent(eventId, 1L, 3L, EventStatus.PENDING);
        AdminUpdateEventDto body = createUpdateEventDto(AdminEventUpdateAction.REJECT_EVENT);

        ViewStatsResponse stats = new ViewStatsResponse(null, "/events/" + eventId, 5L);
        when(statsClient.getStatistics(any(), any(), anyList(), anyBoolean())).thenReturn(List.of(stats));
        when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));

        EventDto result = eventService.updateEvent(eventId, body);

        assertThat(result.getStatus()).isEqualTo(EventStatus.CANCELED);
    }

    private Event createTestEvent(long eventId, long userId, long categoryId, EventStatus status) {
        Category category = createTestCategory(categoryId);
        User initiator = createTestUser(userId);
        LocationEmbeddable location = new LocationEmbeddable(1.0, 1.0);

        return Event.builder()
                .id(eventId)
                .annotation("Test annotation")
                .category(category)
                .confirmedRequests(0)
                .createdOn(LocalDateTime.now())
                .description("Test description")
                .eventDate(LocalDateTime.now().plusHours(3))
                .initiator(initiator)
                .location(location)
                .paid(false)
                .participantLimit(10)
                .publishedOn(null)
                .requestModeration(true)
                .status(status)
                .title("Test Event Title")
                .build();
    }

    private Category createTestCategory(long categoryId) {
        return Category.builder()
                .id(categoryId)
                .name("Test Category")
                .build();
    }

    private User createTestUser(long userId) {
        return User.builder()
                .id(userId)
                .name("Test User")
                .email("test" + userId + "@example.com")
                .build();
    }

    private NewEventDto createTestNewEventDto(Long categoryId, LocalDateTime time) {
        return NewEventDto.builder()
                .annotation("Test annotation for event creation. It must be at least 20 characters long, so we are adding some extra text here to meet the requirement.")
                .category(categoryId)
                .description("Detailed description of the event. Validation requires minimum 20 characters, so this sentence is intentionally long enough to pass the Size constraint.")
                .eventDate(time)
                .location(new Location(55.7558, 37.6176))
                .paid(false)
                .participantLimit(10)
                .requestModeration(true)
                .title("Test Event Title")
                .build();
    }

    private UserUpdateEventDto createTestUpdateEventDto(UserEventUpdateAction action) {
        return UserUpdateEventDto.builder()
                .annotation("Updated annotation text that meets the minimum length of 20 characters requirement for validation.")
                .category(2L)
                .description("Updated detailed description that satisfies the 20–7000 character size constraint.")
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(new Location(59.9343, 30.3351))
                .paid(true)
                .participantLimit(25)
                .requestModeration(false)
                .status(action)
                .title("Updated Event Title")
                .build();
    }

    private RequestDto createTestRequestDto(long requestId, long eventId, long requesterId, RequestStatus status) {
        return RequestDto.builder()
                .id(requestId)
                .event(eventId)
                .requester(requesterId)
                .created(LocalDateTime.now())
                .status(status)
                .build();
    }

    private UpdateRequestStatusDto createTestUpdateRequestStatusDto(List<Long> requestIds, RequestStatus status) {
        return UpdateRequestStatusDto.builder()
                .requestIds(requestIds)
                .status(status)
                .build();
    }

    public AdminUpdateEventDto createUpdateEventDto(AdminEventUpdateAction action) {
        return AdminUpdateEventDto.builder()
                .title("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                .annotation("A".repeat(20))
                .description("A".repeat(20))
                .category(1L)
                .eventDate(LocalDateTime.now().plusDays(1))
                .location(new Location(59.9343, 30.3351))
                .paid(false)
                .participantLimit(0)
                .requestModeration(false)
                .status(action)
                .build();
    }
}
