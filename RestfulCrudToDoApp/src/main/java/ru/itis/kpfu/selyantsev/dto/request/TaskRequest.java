package ru.itis.kpfu.selyantsev.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskRequest {
    @NotBlank(message = "Task Name should not be BLANK!")
    @Size(min = 4, max = 20, message = "Task Name must be in range between 4 and 20 characters")
    private String taskName;
}
