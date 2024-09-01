package ru.itis.kpfu.selyantsev.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.itis.kpfu.selyantsev.dto.request.UserRequest;
import ru.itis.kpfu.selyantsev.dto.response.UserResponse;
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.UserRepository;
import ru.itis.kpfu.selyantsev.service.UserService;
import ru.itis.kpfu.selyantsev.utils.mappers.UserMapper;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public Mono<UUID> create(UserRequest userRequest) {
        User mappedUser = userMapper.toEntity(userRequest);
        return Mono.fromCallable(
                () -> userRepository.save(mappedUser).getUserId()
                ).subscribeOn(Schedulers.boundedElastic());
    }

    @Cacheable(value = "users", key = "#userId")
    @Override
    public Mono<UserResponse> findUserByUserIdWithTasks(UUID userId) {
        return Mono.fromCallable(
                () -> userRepository.findUserByUserIdWithTasks(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId))
        ).subscribeOn(Schedulers.boundedElastic())
        .map(userMapper::toResponse);
    }
    
    @Cacheable(value = "allUsers")
    @Override
    public Flux<UserResponse> findAll(int page, int pageSize) {
        Pageable pageable = PageRequest.of(page - 1, pageSize);

        return Flux.defer(() -> {
            Page<User> userPage = userRepository.findAll(pageable);
            return Flux.fromIterable(userPage.getContent());
        }).map(userMapper::toResponse);
    }

    @CachePut(value = "users", key = "#userId")
    @Override
    public Mono<UserResponse> updateUserById(UUID userId, UserRequest userRequest) {
        User mappedUser = userMapper.toEntity(userRequest);

        return Mono.fromCallable(
                () -> userRepository.findById(userId)
                        .orElseThrow(() -> new UserNotFoundException(userId))
        ).map(foundedUser -> {
            foundedUser.setFirstName(mappedUser.getFirstName());
            foundedUser.setLastName(mappedUser.getLastName());

            return userMapper.toResponse(userRepository.save(foundedUser));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @CacheEvict(value = "users", key = "#userId")
    @Override
    public Mono<Void> deleteUserById(UUID userId) {
        return Mono.fromRunnable(() -> userRepository.deleteById(userId))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
