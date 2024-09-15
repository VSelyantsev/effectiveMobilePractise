package ru.itis.kpfu.selyantsev.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UrlResponse {

    private UUID urlId;
    private String shortUrl;
    private String alias;
    private LocalDateTime expirationDate;
}
