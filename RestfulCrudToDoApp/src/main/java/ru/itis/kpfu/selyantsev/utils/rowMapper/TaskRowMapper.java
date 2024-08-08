package ru.itis.kpfu.selyantsev.utils.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import ru.itis.kpfu.selyantsev.model.Task;

import java.util.UUID;

public class TaskRowMapper {

    private TaskRowMapper() { }

    public static final RowMapper<Task> rowMapper =
            (rs, rowNum) -> Task.builder()
                    .taskId(UUID.fromString(rs.getString("id")))
                    .taskName(rs.getString("task_name"))
                    .isComplete(rs.getBoolean("is_complete"))
                    .userId(UUID.fromString(rs.getString("user_id")))
                    .build();
}
