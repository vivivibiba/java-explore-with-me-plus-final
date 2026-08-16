package ru.practicum.explorewithme.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.controller.priv.PrivateEventController;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.request.ChangedRequestStatusesDto;
import ru.practicum.explorewithme.dto.request.RequestDto;
import ru.practicum.explorewithme.dto.request.RequestStatus;
import ru.practicum.explorewithme.dto.request.UpdateRequestStatusDto;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.service.EventService;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@WebMvcTest(PrivateEventController.class)
public class PrivateEventControllerTests {
    private static final String URL_BASE = ACCESS_PRIVATE + "/{" + ID_USER + "}";
    private static final String URL_BASE_EVENTS = URL_BASE + URL_EVENTS;
    private static final String URL_BASE_EVENTS_ID = URL_BASE_EVENTS + "/{" + ID_EVENT + "}";

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EventService eventService;

    private NewEventDto postRequest;
    private UserUpdateEventDto updateRequest;
    private EventDto response;
    private UpdateRequestStatusDto update;
    private RequestDto responseRequest;
    private ChangedRequestStatusesDto responseStatuses;

    @BeforeEach
    void setUp() {
        LocalDateTime time = LocalDateTime.of(2026, 8, 10, 14, 0, 0);
        LocalDateTime created = LocalDateTime.of(2026, 7, 10, 14, 0, 0);
        Location location = new Location(1, 1);
        UserShortDto initiator = UserShortDto.builder()
                .id(1L)
                .name("Name")
                .build();
        CategoryDto category = CategoryDto.builder()
                .id(1L)
                .name("Category")
                .build();

        postRequest = NewEventDto.builder()
                .annotation("AnnotationAnnotationAnnotationAnnotation")
                .category(1L)
                .description("DescriptionDescriptionDescriptionDescription")
                .eventDate(time)
                .location(location)
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .title("TitleTitleTitleTitleTitle")
                .build();

        updateRequest = UserUpdateEventDto.builder()
                .annotation("AnnotationAnnotationAnnotationAnnotation")
                .category(1L)
                .description("DescriptionDescriptionDescriptionDescription")
                .eventDate(time)
                .location(location)
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .status(UserEventUpdateAction.SEND_TO_REVIEW)
                .title("TitleTitleTitleTitleTitle")
                .build();

        response = EventDto.builder()
                .id(1L)
                .annotation("AnnotationAnnotationAnnotationAnnotation")
                .category(category)
                .confirmedRequests(10)
                .createdOn(created)
                .description("DescriptionDescriptionDescriptionDescription")
                .eventDate(time)
                .initiator(initiator)
                .location(location)
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .status(EventStatus.PENDING)
                .title("TitleTitleTitleTitleTitle")
                .build();

        update = UpdateRequestStatusDto.builder()
                .requestIds(List.of(1L))
                .status(RequestStatus.CONFIRMED)
                .build();

        responseRequest = RequestDto.builder()
                .build();

        responseStatuses = ChangedRequestStatusesDto.builder()
                .confirmedRequests(null)
                .build();
    }

    @Test
    public void createEvent() throws Exception {
        long userId = 1L;

        when(eventService.createEvent(userId, postRequest))
                .thenReturn(response);

        mvc.perform(post(URL_BASE_EVENTS, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    public void getEvents() throws Exception {
        long userId = 1L;

        when(eventService.getEvents(userId, 1, 1))
                .thenReturn(List.of(response));

        mvc.perform(get(URL_BASE_EVENTS, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void getEvent() throws Exception {
        long userId = 1L;
        long eventId = 1L;

        when(eventService.getEvent(userId, eventId))
                .thenReturn(response);

        mvc.perform(get(URL_BASE_EVENTS, userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void updateEvent() throws Exception {
        long userId = 1L;
        long eventId = 1L;

        when(eventService.updateEvent(userId, eventId, updateRequest))
                .thenReturn(response);

        mvc.perform(patch(URL_BASE_EVENTS_ID, userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postRequest)))
                .andExpect(status().isOk());
    }

    @Test
    public void getRequest() throws Exception {
        long userId = 1L;
        long eventId = 1L;

        when(eventService.getRequests(userId, eventId))
                .thenReturn(List.of(responseRequest));

        mvc.perform(get(URL_BASE_EVENTS_ID + URL_REQUESTS, userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void updateRequest() throws Exception {
        long userId = 1L;
        long eventId = 1L;

        when(eventService.updateRequestStatuses(userId, eventId, update))
                .thenReturn(responseStatuses);

        mvc.perform(patch(URL_BASE_EVENTS_ID + URL_REQUESTS, userId, eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk());
    }
}
