package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.itis.kpfu.selyantsev.dto.request.UrlRequest;
import ru.itis.kpfu.selyantsev.dto.response.UrlResponse;
import ru.itis.kpfu.selyantsev.exceptions.AliasAlreadyExistException;
import ru.itis.kpfu.selyantsev.exceptions.ExpiredLink;
import ru.itis.kpfu.selyantsev.exceptions.UrlNotFoundException;
import ru.itis.kpfu.selyantsev.model.Url;
import ru.itis.kpfu.selyantsev.repository.UrlRepository;
import ru.itis.kpfu.selyantsev.service.UrlService;
import ru.itis.kpfu.selyantsev.util.mapper.UrlMapper;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;

import static ru.itis.kpfu.selyantsev.util.HashUtil.generateShortUrl;
import static ru.itis.kpfu.selyantsev.util.HashUtil.hashWithMD5;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlRepository urlRepository;
    private final UrlMapper urlMapper;

    @Override
    public UrlResponse create(UrlRequest urlRequest) throws NoSuchAlgorithmException {
        Url url = Url.builder().build();

        String alias = urlRequest.getAlias();
        Long ttl = urlRequest.getTtl();
        String shortUrl;
        String fullHash = null;

        // if alias is not null we do not need fullHash
        if (alias != null) {
            if (urlRepository.existsByShortUrl(alias)) {
                throw new AliasAlreadyExistException(alias);
            }
            shortUrl = alias;
        } else {
            // if alias is null we need fullHash
            fullHash = hashWithMD5(urlRequest.getOriginalUrl());
            shortUrl = generateShortUrl(fullHash);
        }

        url.setOriginalUrl(urlRequest.getOriginalUrl());
        url.setShortUrl(shortUrl);

        if (fullHash != null) {
            url.setFullHash(fullHash);
        }

        if (ttl !=  null) {
            url.setExpirationDate(LocalDateTime.now().plusSeconds(ttl));
        }

        urlRepository.save(url);

        return urlMapper.toResponse(url);
    }

    @Override
    public String redirectToOriginalUrl(String shortUrl) {
        Url entity = urlRepository.findByShortUrl(shortUrl)
                .orElseThrow(() -> new UrlNotFoundException(shortUrl));

        if (entity.getExpirationDate() != null && entity.getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new ExpiredLink(shortUrl);
        }

        return entity.getOriginalUrl();
    }

    @Override
    @Scheduled(fixedRate = 60000)
    public void deleteExpiredUrls() {
        urlRepository.deleteByExpirationDate(LocalDateTime.now());
    }
}
