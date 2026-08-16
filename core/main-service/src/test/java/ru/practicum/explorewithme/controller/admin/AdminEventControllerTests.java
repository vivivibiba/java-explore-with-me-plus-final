package ru.practicum.explorewithme.controller.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.explorewithme.dto.category.CategoryDto;
import ru.practicum.explorewithme.dto.event.*;
import ru.practicum.explorewithme.dto.user.UserShortDto;
import ru.practicum.explorewithme.service.EventService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.practicum.explorewithme.controller.ControllerConstants.*;

@WebMvcTest(AdminEventController.class)
public class AdminEventControllerTests {
    private static final String URL_BASE = ACCESS_ADMIN + URL_EVENTS;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private EventService eventService;

    private AdminUpdateEventDto updateRequest;
    private EventDto responseEvent;
    private List<EventDto> responseList;

    @BeforeEach
    void setUp() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        Location location = new Location(1, 1);
        CategoryDto categoryDto = CategoryDto.builder()
                .id(1L)
                .name("Name")
                .build();

        UserShortDto initiator = UserShortDto.builder()
                .id(1L)
                .name("AdminName")
                .build();

        updateRequest = AdminUpdateEventDto.builder()
                .title("ValidTitle123456789012")
                .annotation("A".repeat(20))
                .description("B".repeat(20))
                .category(1L)
                .eventDate(time)
                .location(location)
                .paid(false)
                .participantLimit(10)
                .requestModeration(false)
                .status(AdminEventUpdateAction.PUBLISH_EVENT)
                .build();

        responseEvent = EventDto.builder()
                .id(1L)
                .annotation("AnnotationAnnotationAnnotationAnnotation")
                .category(categoryDto)
                .confirmedRequests(10)
                .createdOn(created)
                .description("DescriptionDescriptionDescriptionDescription")
                .eventDate(time)
                .initiator(initiator)
                .location(location)
                .paid(true)
                .participantLimit(10)
                .requestModeration(true)
                .status(EventStatus.PUBLISHED)
                .title("TitleTitleTitleTitleTitle")
                .views(42L)
                .build();

        responseList = List.of(responseEvent);
    }

    @Test
    void searchEvents_defaultParams_returnsOkAndList() throws Exception {
        when(eventService.searchEvents(null, null, null, null, null, 0, 10))
                .thenReturn(responseList);

        mvc.perform(get(URL_BASE))
                .andExpect(status().isOk());
    }

    @Test
    void searchEvents_withFiltersAndPagination_returnsOkAndFilteredList() throws Exception {
        List<Long> users = List.of(1L, 2L);
        List<String> states = List.of("PUBLISHED", "CANCELLED");
        List<Long> categories = List.of(3L);
        String rangeStart = "2024-01-01 00:00:00";
        String rangeEnd = "2024-12-31 23:59:59";
        int from = 5;
        int size = 10;

        when(eventService.searchEvents(users, states, categories, rangeStart, rangeEnd, from, size))
                .thenReturn(responseList);

        mvc.perform(get(URL_BASE)
                        .param("users", "1", "2")
                        .param("states", "PUBLISHED", "CANCELLED")
                        .param("categories", "3")
                        .param("rangeStart", rangeStart)
                        .param("rangeEnd", rangeEnd)
                        .param("from", String.valueOf(from))
                        .param("size", String.valueOf(size)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void searchEvents_emptyResult_returnsEmptyList() throws Exception {
        when(eventService.searchEvents(any(), any(), any(), anyString(), anyString(), anyInt(), anyInt()))
                .thenReturn(Collections.emptyList());

        mvc.perform(get(URL_BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void updateEvent_validDto_returnsOk() throws Exception {
        long eventId = 1L;

        when(eventService.updateEvent(eq(eventId), any(AdminUpdateEventDto.class)))
                .thenReturn(responseEvent);

        String content = objectMapper.writeValueAsString(updateRequest);

        mvc.perform(patch(URL_BASE + "/{" + ID_EVENT + "}", eventId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.state").value("PUBLISHED"));
    }
}
