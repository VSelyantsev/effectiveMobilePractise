package ru.itis.kpfu.selyantsev.junit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.itis.kpfu.selyantsev.controller.UrlController;
import ru.itis.kpfu.selyantsev.dto.request.UrlRequest;
import ru.itis.kpfu.selyantsev.dto.response.UrlResponse;
import ru.itis.kpfu.selyantsev.service.UrlService;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UrlController.class)
public class UrlControllerJunitTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UrlService urlService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreate_shouldReturnHttpStatusCreated() throws Exception {
        UrlRequest request = UrlRequest.builder()
                .originalUrl("http://example.com")
                .alias("shortExample")
                .build();

        UrlResponse response = UrlResponse.builder()
                .shortUrl("shortExample")
                .build();

        when(urlService.create(request)).thenReturn(response);

        mockMvc.perform(post("/api/v1/urls")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void testRedirectToOriginalUrl_shouldReturnHttpStatusFound() throws Exception {
        String shortUrl = "shortExample";
        String originalUrl = "http://example.com";

        when(urlService.redirectToOriginalUrl(shortUrl)).thenReturn(originalUrl);

        mockMvc.perform(get("/api/v1/urls/" + shortUrl))
                .andExpect(status().isFound())
                .andExpect(header().string("Location", originalUrl));
    }
}
