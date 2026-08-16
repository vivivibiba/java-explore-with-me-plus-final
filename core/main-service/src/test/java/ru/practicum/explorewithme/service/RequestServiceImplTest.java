package ru.practicum.explorewithme.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.explorewithme.dto.event.EventStatus;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;
import ru.practicum.explorewithme.entity.Event;
import ru.practicum.explorewithme.entity.Request;
import ru.practicum.explorewithme.entity.User;
import ru.practicum.explorewithme.exception.DuplicatedDataException;
import ru.practicum.explorewithme.exception.Entities;
import ru.practicum.explorewithme.exception.UnavailableUpdateException;
import ru.practicum.explorewithme.repository.EventRepository;
import ru.practicum.explorewithme.repository.RequestRepository;
import ru.practicum.explorewithme.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class RequestServiceImplTest {
    @Mock
    private RequestRepository requestRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RequestServiceImpl requestService;

    private static final long EVENT_ID = 1L;
    private static final long USER_ID = 2L;
    private static final long REQUEST_ID = 3L;

    @Test
    void create_success() {
        Event event = new Event();
        event.setId(EVENT_ID);
        event.setInitiator(new User());
        event.getInitiator().setId(99L);
        event.setStatus(EventStatus.PUBLISHED);
        event.setParticipantLimit(10);
        event.setConfirmedRequests(2);
        event.setRequestModeration(false);

        User user = new User();
        user.setId(USER_ID);

        Request savedRequest = new Request();
        savedRequest.setId(REQUEST_ID);
        savedRequest.setCreated(LocalDateTime.now());
        savedRequest.setEvent(event);
        savedRequest.setRequester(user);
        savedRequest.setStatus(RequestStatus.CONFIRMED);

        when(requestRepository.existsByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(false);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(requestRepository.save(any(Request.class))).thenReturn(savedRequest);
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RequestDto dto = requestService.create(USER_ID, EVENT_ID);

        assertThat(dto.getId()).isEqualTo(REQUEST_ID);
        assertThat(dto.getStatus()).isEqualTo(RequestStatus.CONFIRMED);
        verify(requestRepository, times(1)).existsByEventIdAndRequesterId(EVENT_ID, USER_ID);
        verify(eventRepository, times(1)).findById(EVENT_ID);
        verify(userRepository, times(1)).findById(USER_ID);
        verify(requestRepository, times(1)).save(any(Request.class));
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void create_duplicateRequest_throws() {
        when(requestRepository.existsByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(true);

        assertThatThrownBy(() -> requestService.create(USER_ID, EVENT_ID))
                .isInstanceOf(DuplicatedDataException.class)
                .hasMessageContaining(Entities.REQUEST.name());

        verify(requestRepository, times(1)).existsByEventIdAndRequesterId(EVENT_ID, USER_ID);
        verify(eventRepository, never()).findById(anyLong());
        verify(userRepository, never()).findById(anyLong());
    }

    @Test
    void create_unpublishedEvent_throws() {
        Event event = new Event();
        event.setId(EVENT_ID);
        event.setInitiator(new User());
        event.getInitiator().setId(99L);
        event.setStatus(EventStatus.PENDING);
        event.setParticipantLimit(10);
        event.setConfirmedRequests(0);

        User user = new User();
        user.setId(USER_ID);

        when(requestRepository.existsByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(false);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> requestService.create(USER_ID, EVENT_ID))
                .isInstanceOf(UnavailableUpdateException.class);

        verify(requestRepository, times(1)).existsByEventIdAndRequesterId(EVENT_ID, USER_ID);
        verify(eventRepository, times(1)).findById(EVENT_ID);
    }

    @Test
    void create_eventLimitReached_throws() {
        Event event = new Event();
        event.setId(EVENT_ID);
        event.setStatus(EventStatus.PUBLISHED);
        event.setInitiator(new User());
        event.getInitiator().setId(99L);
        event.setParticipantLimit(5);
        event.setConfirmedRequests(5);
        event.setRequestModeration(true);

        User user = new User();
        user.setId(USER_ID);

        when(requestRepository.existsByEventIdAndRequesterId(anyLong(), anyLong())).thenReturn(false);
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> requestService.create(USER_ID, EVENT_ID))
                .isInstanceOf(UnavailableUpdateException.class);

        verify(requestRepository, times(1)).existsByEventIdAndRequesterId(EVENT_ID, USER_ID);
        verify(eventRepository, times(1)).findById(EVENT_ID);
    }

    @Test
    void cancelRequest_confirmedStatus_throws() {
        Request request = new Request();
        request.setId(REQUEST_ID);
        request.setStatus(RequestStatus.CONFIRMED);

        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));

        assertThatThrownBy(() -> requestService.cancelRequest(USER_ID, REQUEST_ID))
                .isInstanceOf(DuplicatedDataException.class)
                .hasMessageContaining("status");

        verify(requestRepository, times(1)).findById(REQUEST_ID);
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void cancelRequest_success() {
        Event event = new Event();
        event.setId(EVENT_ID);
        event.setConfirmedRequests(3);

        User requester = new User();
        requester.setId(USER_ID);

        Request request = new Request();
        request.setId(REQUEST_ID);
        request.setStatus(RequestStatus.PENDING);
        request.setEvent(event);
        request.setRequester(requester);

        when(requestRepository.findById(anyLong())).thenReturn(Optional.of(request));
        when(eventRepository.findById(anyLong())).thenReturn(Optional.of(event));
        when(requestRepository.save(any())).thenReturn(request);

        RequestDto dto = requestService.cancelRequest(USER_ID, REQUEST_ID);

        assertThat(dto.getStatus()).isEqualTo(RequestStatus.CANCELED);
        verify(requestRepository, times(1)).findById(REQUEST_ID);
        verify(requestRepository, times(1)).save(request);
        verify(eventRepository, times(1)).findById(event.getId());
        verify(eventRepository, times(1)).save(event);
    }
}
