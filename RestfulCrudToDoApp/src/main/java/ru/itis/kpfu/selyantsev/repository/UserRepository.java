package ru.itis.kpfu.selyantsev.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.model.User;

import java.util.UUID;

public interface UserRepository {
    Mono<UUID> create(User user);
    Mono<User> findUserById(UUID userId);
    Flux<User> findAll();
    Mono<User> updateUserById(UUID userId, User user);
    Mono<Void> deleteUserById(UUID userId);

}
