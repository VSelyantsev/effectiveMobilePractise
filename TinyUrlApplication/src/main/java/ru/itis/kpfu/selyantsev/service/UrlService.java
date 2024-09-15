package ru.itis.kpfu.selyantsev.service;

import ru.itis.kpfu.selyantsev.dto.request.UrlRequest;
import ru.itis.kpfu.selyantsev.dto.response.UrlResponse;

import java.security.NoSuchAlgorithmException;

public interface UrlService {

    UrlResponse create(UrlRequest urlRequest) throws NoSuchAlgorithmException;

    String redirectToOriginalUrl(String shortUrl);

    void deleteExpiredUrls();
}
