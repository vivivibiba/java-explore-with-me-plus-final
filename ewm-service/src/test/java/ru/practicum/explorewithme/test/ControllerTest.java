package ru.practicum.explorewithme.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

public class ControllerTest extends TestBase {
    protected MockMvc mockMvc;

    private final ObjectMapper mapper = createConfiguredMapper();

    private static ObjectMapper createConfiguredMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    protected ResultActions performPost(String url, Object body) {
        try {
            return mockMvc.perform(MockMvcRequestBuilders.post(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(body)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ResultActions performDelete(String url) {
        try {
            return mockMvc.perform(MockMvcRequestBuilders.delete(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ResultActions performPatch(String url, Object body) {
        try {
            return mockMvc.perform(MockMvcRequestBuilders.patch(url)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(body)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected ResultActions performGet(String url) {
        try {
            return mockMvc.perform(MockMvcRequestBuilders.get(url));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void expectStatusOk(ResultActions actions) {
        expect(actions, MockMvcResultMatchers.status().isOk());
    }

    private void expect(ResultActions actions, ResultMatcher matcher) {
        try {
            actions.andExpect(matcher);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void expectStatusCreated(ResultActions actions) {
        expect(actions, MockMvcResultMatchers.status().isCreated());
    }

    protected void expectStatusNoContent(ResultActions actions) {
        expect(actions, MockMvcResultMatchers.status().isNoContent());
    }

    protected String createIdUrl(String urlBase, long id) {
        return String.format("%s/%d", urlBase, id);
    }
}
