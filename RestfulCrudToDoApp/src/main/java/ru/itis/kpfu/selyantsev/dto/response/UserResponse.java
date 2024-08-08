package ru.itis.kpfu.selyantsev.dto.response;

import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserResponse {
    private UUID userId;
    private String firstName;
    private String lastName;
    List<TaskResponse> taskList;
}
