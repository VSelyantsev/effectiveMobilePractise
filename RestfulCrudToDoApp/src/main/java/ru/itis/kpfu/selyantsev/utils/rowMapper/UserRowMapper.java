package ru.itis.kpfu.selyantsev.utils.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import ru.itis.kpfu.selyantsev.model.User;

import java.util.UUID;

public class UserRowMapper {

    private UserRowMapper() { }

    public static final RowMapper<User> rowMapper =
            (rs, rowNum) -> User.builder()
                    .userId(UUID.fromString(rs.getString("id")))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .build();
}
