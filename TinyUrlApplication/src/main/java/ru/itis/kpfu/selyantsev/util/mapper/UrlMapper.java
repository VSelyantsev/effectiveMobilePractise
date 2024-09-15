package ru.itis.kpfu.selyantsev.util.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.itis.kpfu.selyantsev.dto.response.UrlResponse;
import ru.itis.kpfu.selyantsev.model.Url;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UrlMapper {

    @Mapping(target = "alias", ignore = true)
    UrlResponse toResponse(Url url);
}
