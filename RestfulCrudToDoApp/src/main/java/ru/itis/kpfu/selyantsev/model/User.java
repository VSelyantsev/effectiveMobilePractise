package ru.itis.kpfu.selyantsev.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private UUID userId;
    private String firstName;
    private String lastName;
}
