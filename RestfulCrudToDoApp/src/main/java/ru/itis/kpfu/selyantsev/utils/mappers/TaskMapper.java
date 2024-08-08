package ru.itis.kpfu.selyantsev.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itis.kpfu.selyantsev.dto.request.TaskRequest;
import ru.itis.kpfu.selyantsev.dto.response.TaskResponse;
import ru.itis.kpfu.selyantsev.model.Task;

@Mapper(componentModel = "spring", imports = {UserMapper.class})
public interface TaskMapper {

    @Mapping(target = "taskId", expression = "java(UserMapper.getRandomUserUUID())")
    @Mapping(target = "userId", ignore = true)
    @Mapping(target = "isComplete", expression = "java(TaskMapper.setDefaultNotComplete())")
    Task toEntity(TaskRequest taskRequest);

    @Mapping(target = "isComplete", source = "complete")
    TaskResponse toResponse(Task task);

    static boolean setDefaultNotComplete() {
        return false;
    }
}
