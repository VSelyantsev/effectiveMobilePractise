package ru.itis.kpfu.selyantsev.repository;

import reactor.core.publisher.Flux;
import ru.itis.kpfu.selyantsev.model.Task;

import java.util.UUID;

public interface TaskRepository extends CrudRepository<Task, UUID> {
    Flux<Task> findAllTasksByUserId(UUID userId);
}
