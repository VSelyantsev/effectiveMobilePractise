package ru.itis.kpfu.selyantsev.dto.request;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UrlRequest {
    private String originalUrl;
    private String alias;
    private Long ttl;
}
