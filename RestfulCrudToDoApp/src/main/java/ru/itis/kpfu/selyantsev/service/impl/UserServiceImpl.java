package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.exceptions.FailedExecuteOperation;
import ru.itis.kpfu.selyantsev.exceptions.InvalidIdException;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.CrudRepository;
import ru.itis.kpfu.selyantsev.service.UserService;
import ru.itis.kpfu.selyantsev.utils.mappers.UserMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final CrudRepository<User, UUID> userRepository;
    private final UserMapper userMapper;

    @Override
    public Mono<UUID> create(UserRequest userRequest) {
        User mappedUser = userMapper.toEntity(userRequest);
        return userRepository.create(mappedUser)
                .flatMap(uuid -> {
                    if (uuid == null) {
                        return Mono.error(new FailedExecuteOperation(mappedUser.getUserId()));
                    }
                    return Mono.just(uuid);
                });
    }

    @Cacheable(value = "users", key = "#userId")
    @Override
    public Mono<UserResponse> findUserById(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new InvalidIdException(userId)))
                .flatMap(user -> {
                    if (user == null) {
                        return Mono.error(new UserNotFoundException(userId));
                    }
                    return Mono.just(user);
                })
                .map(userMapper::toResponse);
    }

    @Cacheable(value = "allUsers")
    @Override
    public Flux<UserResponse> findAll(int page, int pageSize) {
        return userRepository.findAll(page, pageSize)
                .map(userMapper::toResponse);
    }

    @CachePut(value = "users", key = "#userId")
    @Override
    public Mono<UserResponse> updateUserById(UUID userId, UserRequest userRequest) {
        User mappedUser = userMapper.toEntity(userRequest);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new InvalidIdException(userId)))
                .flatMap(existingUser -> {

                    if (existingUser == null) {
                        return Mono.error(new UserNotFoundException(userId));
                    }

                    existingUser.setFirstName(mappedUser.getFirstName());
                    existingUser.setLastName(mappedUser.getLastName());

                    return userRepository.update(existingUser)
                            .map(userMapper::toResponse);
                });
    }

    @CacheEvict(value = "users", key = "#userId")
    @Override
    public Mono<Void> deleteUserById(UUID userId) {
        return userRepository.deleteById(userId);
    }
}
