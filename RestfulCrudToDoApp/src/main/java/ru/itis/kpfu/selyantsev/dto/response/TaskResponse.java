package ru.itis.kpfu.selyantsev.dto.response;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskResponse {
    private UUID taskId;
    private String taskName;
    private boolean isComplete;
    private UUID userId;
}
