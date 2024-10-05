package ru.itis.kpfu.selyantsev.util;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.itis.kpfu.selyantsev.dto.UserRequest;
import ru.itis.kpfu.selyantsev.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "userId", ignore = true)
    User toEntity(UserRequest userRequest);
}
