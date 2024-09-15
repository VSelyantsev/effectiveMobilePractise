package ru.itis.kpfu.selyantsev.junit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.itis.kpfu.selyantsev.dto.request.UrlRequest;
import ru.itis.kpfu.selyantsev.dto.response.UrlResponse;
import ru.itis.kpfu.selyantsev.exceptions.AliasAlreadyExistException;
import ru.itis.kpfu.selyantsev.exceptions.ExpiredLink;
import ru.itis.kpfu.selyantsev.exceptions.UrlNotFoundException;
import ru.itis.kpfu.selyantsev.model.Url;
import ru.itis.kpfu.selyantsev.repository.UrlRepository;
import ru.itis.kpfu.selyantsev.service.impl.UrlServiceImpl;
import ru.itis.kpfu.selyantsev.util.mapper.UrlMapper;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UrlServiceJunitTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private UrlMapper urlMapper;

    @InjectMocks
    private UrlServiceImpl urlService;


    @Test
    void testCreateWithAlias_Success() throws NoSuchAlgorithmException {
        UrlRequest request = new UrlRequest("http://exapmple.com", "myFirstShortLink", null);

        Url expectedUrl = Url.builder()
                .originalUrl("http://exapmple.com")
                .shortUrl("myFirstShortLink")
                .build();

        UrlResponse expectedResponse = UrlResponse.builder()
                .shortUrl("http://exapmple.com")
                .alias("myFirstShortLink")
                .build();


        when(urlRepository.existsByShortUrl(request.getAlias())).thenReturn(false);
        when(urlRepository.save(any(Url.class))).thenReturn(expectedUrl);
        when(urlMapper.toResponse(any(Url.class))).thenReturn(expectedResponse);

        UrlResponse actualResponse = urlService.create(request);

        assertThat(actualResponse.getAlias()).isEqualTo("myFirstShortLink");
        verify(urlRepository, times(1)).existsByShortUrl(anyString());
        verify(urlRepository, times(1)).save(any(Url.class));
        verify(urlMapper, times(1)).toResponse(any(Url.class));
    }

    @Test
    void testCreateWithAlias_AliasAlreadyExistException() {
        UrlRequest request = new UrlRequest("http://exapmple.com", "myFirstShortLink", null);

        when(urlRepository.existsByShortUrl(request.getAlias())).thenReturn(true);

        assertThatThrownBy(() -> urlService.create(request))
                .isInstanceOf(AliasAlreadyExistException.class)
                .hasMessageContaining(request.getAlias());

        verify(urlRepository, times(1)).existsByShortUrl(request.getAlias());
        verify(urlRepository, never()).save(any(Url.class));
        verify(urlMapper, never()).toResponse(any(Url.class));
    }

    @Test
    void testCreateWithoutAlias_Success() throws NoSuchAlgorithmException {
        UrlRequest request = new UrlRequest("http://exapmple.com", null, null);

        Url expectedUrl = Url.builder()
                .originalUrl("http://exapmple.com")
                .shortUrl("generatedShortUrl")
                .build();

        UrlResponse expectedResponse = UrlResponse.builder()
                .shortUrl("http://exapmple.com")
                .alias("generatedShortUrl")
                .build();

        when(urlRepository.save(any(Url.class))).thenReturn(expectedUrl);
        when(urlMapper.toResponse(any(Url.class))).thenReturn(expectedResponse);

        UrlResponse actualResponse = urlService.create(request);

        assertThat(actualResponse.getAlias()).isEqualTo("generatedShortUrl");
        verify(urlRepository, times(1)).save(any(Url.class));
        verify(urlMapper, times(1)).toResponse(any(Url.class));
    }

    @Test
    void testCreateWithTTL_Success() throws NoSuchAlgorithmException {
        UrlRequest request = new UrlRequest("https://example.com", null, 3600L);
        Url expectedUrl = Url.builder()
                .originalUrl("https://example.com")
                .shortUrl("generatedShortUrl")
                .fullHash("fullHashValue")
                .build();

        UrlResponse expectedResponse = UrlResponse.builder()
                .shortUrl("http://exapmple.com")
                .alias("generatedShortUrl")
                .expirationDate(LocalDateTime.now().plusSeconds(3600))
                .build();

        when(urlRepository.save(any(Url.class))).thenReturn(expectedUrl);
        when(urlMapper.toResponse(any(Url.class))).thenReturn(expectedResponse);

        UrlResponse response = urlService.create(request);

        assertThat(response.getExpirationDate()).isNotNull();
        verify(urlRepository).save(any(Url.class));
        verify(urlMapper).toResponse(any(Url.class));
    }

    @Test
    void testRedirectToOriginalUrl_Success() {
        String shortUrl = "shortExample";
        Url entity = Url.builder()
                .shortUrl(shortUrl)
                .originalUrl("http://example.com")
                .expirationDate(LocalDateTime.now().plusDays(1))
                .build();

        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(entity));

        String actualUrl = urlService.redirectToOriginalUrl(shortUrl);

        assertThat(actualUrl).isEqualTo("http://example.com");
        verify(urlRepository, times(1)).findByShortUrl(shortUrl);
    }

    @Test
    void testRedirectToOriginalUrl_NotFound() {
        String shortUrl = "shortExample";

        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> urlService.redirectToOriginalUrl(shortUrl))
                .isInstanceOf(UrlNotFoundException.class)
                .hasMessageContaining(shortUrl);

        verify(urlRepository, times(1)).findByShortUrl(shortUrl);
    }

    @Test
    void testRedirectToOriginalUrl_ExpiredLink() {
        String shortUrl = "shortExample";
        Url expiredLink = Url.builder()
                .shortUrl(shortUrl)
                .originalUrl("http://example.com")
                .expirationDate(LocalDateTime.now().minusDays(1))
                .build();

        when(urlRepository.findByShortUrl(shortUrl)).thenReturn(Optional.of(expiredLink));

        assertThatThrownBy(() -> urlService.redirectToOriginalUrl(shortUrl))
                .isInstanceOf(ExpiredLink.class)
                .hasMessageContaining(shortUrl);

        verify(urlRepository, times(1)).findByShortUrl(shortUrl);
    }

    @Test
    void testDeleteExpiredUrls_Success() {
        urlService.deleteExpiredUrls();

        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(urlRepository).deleteByExpirationDate(captor.capture());

        LocalDateTime capturedTime = captor.getValue();
        assertThat(capturedTime).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }

}
