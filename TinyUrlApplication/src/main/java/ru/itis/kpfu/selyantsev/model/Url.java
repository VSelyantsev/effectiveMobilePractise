package ru.itis.kpfu.selyantsev.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity(name = "t_url")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Url {

    @Id
    @UuidGenerator
    private UUID urlId;
    private String shortUrl;
    private String fullHash;
    private String originalUrl;
    private LocalDateTime expirationDate;
}

