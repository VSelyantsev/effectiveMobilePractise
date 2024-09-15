package ru.itis.kpfu.selyantsev.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import ru.itis.kpfu.selyantsev.config.ContainerConfiguration;
import ru.itis.kpfu.selyantsev.dto.request.UrlRequest;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.yml")
@ActiveProfiles("test")
public class UrlControllerTest {

    @Container
    PostgreSQLContainer<?> container = ContainerConfiguration.getInstance();

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String API_PATH = "/api/v1/urls";

    @Test
    @Sql(scripts = {"classpath:/sql/url.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testCreateUrl_Success() throws Exception {
        UrlRequest request = UrlRequest.builder()
                .originalUrl("http://example.com")
                .alias("myShortAlias")
                .build();

        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.shortUrl", is("myShortAlias")));
    }

    @Test
    @Sql(scripts = {"classpath:/sql/url.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testCreateUrlWithAlreadyExistAlias_shouldThrowAliasAlreadyExistException() throws Exception {
        UrlRequest request = UrlRequest.builder()
                .originalUrl("http://example.com")
                .alias("alias")
                .build();

        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(content().string(containsString(request.getAlias())));
    }

    @Test
    @Sql(scripts = {"classpath:/sql/url.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testCreateWithoutAliasAndWithTTL_Success() throws Exception {
        UrlRequest request = UrlRequest.builder()
                .originalUrl("http://example.com")
                .ttl(400L)
                .build();

        mockMvc.perform(post(API_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    @Sql(scripts = {"classpath:/sql/url.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testRedirectToOriginalUrl_Success() throws Exception {
        String shortUrl = "alias";

        mockMvc.perform(get(API_PATH + "/{shortUrl}", shortUrl))
                .andExpect(status().isFound());
    }

    @Test
    @Sql(scripts = {"classpath:/sql/url.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    void testRedirectToOriginalUrl_shouldReturn404Status() throws Exception {
        String notExistAlias = "notExistAliasInDb";

        mockMvc.perform(get(API_PATH + "/{shortUrl}", notExistAlias))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString(notExistAlias)));
    }

    @Test
    void testRedirectToOriginalUrlWithExpiredAlias_shouldReturnGoneStatus() throws Exception {
        String expiredAlias = "expiredAlias";

        mockMvc.perform(get(API_PATH + "/{shortUrl}", expiredAlias))
                .andExpect(status().isGone())
                .andExpect(content().string(containsString(expiredAlias)));
    }
}
