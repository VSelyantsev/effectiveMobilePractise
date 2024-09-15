package ru.itis.kpfu.selyantsev.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.itis.kpfu.selyantsev.api.UrlApi;
import ru.itis.kpfu.selyantsev.dto.request.UrlRequest;
import ru.itis.kpfu.selyantsev.dto.response.UrlResponse;
import ru.itis.kpfu.selyantsev.service.UrlService;

import java.net.URI;
import java.security.NoSuchAlgorithmException;

@RestController
@RequiredArgsConstructor
public class UrlController implements UrlApi {

    private final UrlService urlService;

    @Override
    public ResponseEntity<UrlResponse> create(UrlRequest urlRequest) throws NoSuchAlgorithmException {
        return ResponseEntity.status(HttpStatus.CREATED).body(urlService.create(urlRequest));
    }

    @Override
    public ResponseEntity<Void> redirectToOriginalUrl(String shortUrl) {
        String originalUrl = urlService.redirectToOriginalUrl(shortUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(originalUrl))
                .build();
    }
}
