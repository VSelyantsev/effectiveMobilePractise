package ru.itis.kpfu.selyantsev.utils.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.model.User;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", expression = "java(UserMapper.getRandomUserUUID())")
    User toEntity(UserRequest userRequest);

    @Mapping(target = "taskList", ignore = true)
    UserResponse toResponse(User user);

    static UUID getRandomUserUUID() {
        return UUID.randomUUID();
    }

}
