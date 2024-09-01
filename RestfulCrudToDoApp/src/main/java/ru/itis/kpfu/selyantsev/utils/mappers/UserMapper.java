package ru.itis.kpfu.selyantsev.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.model.User;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "taskList", ignore = true)
    User toEntity(UserRequest userRequest);

    @Mapping(target = "taskList", expression = "java(UserMapper.getTaskResponses(user))")
    UserResponse toResponse(User user);

    static List<TaskResponse> getTaskResponses(User user) {
        return user.getTaskList()
                .stream()
                .map(task ->
                    TaskResponse.builder()
                            .taskId(task.getTaskId())
                            .taskName(task.getTaskName())
                            .isComplete(task.isComplete())
                            .userId(task.getUser().getUserId())
                            .build()
                ).toList();
    }
}
