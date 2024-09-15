package ru.itis.kpfu.selyantsev.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.itis.kpfu.selyantsev.dto.request.UrlRequest;
import ru.itis.kpfu.selyantsev.dto.response.UrlResponse;

import java.security.NoSuchAlgorithmException;

@RequestMapping(value = "/api/v1/urls")
public interface UrlApi {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    ResponseEntity<UrlResponse> create(@RequestBody UrlRequest urlRequest) throws NoSuchAlgorithmException;

    @GetMapping(value = "/{shortUrl}")
    ResponseEntity<Void> redirectToOriginalUrl(@PathVariable(name = "shortUrl") String shortUrl);

}
