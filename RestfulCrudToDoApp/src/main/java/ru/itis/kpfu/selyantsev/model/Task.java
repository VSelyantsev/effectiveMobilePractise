package ru.itis.kpfu.selyantsev.model;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Task {
    private UUID taskId;
    private String taskName;
    private boolean isComplete;
    private UUID userId;
}
