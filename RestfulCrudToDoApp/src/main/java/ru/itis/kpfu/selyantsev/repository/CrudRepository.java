package ru.itis.kpfu.selyantsev.repository;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CrudRepository<T, ID> {

    Mono<ID> create(T t);

    Mono<T> findById(ID id);

    Flux<T> findAll(int page, int pageSize);

    Mono<T> update(T t);

    Mono<Void> deleteById(ID id);
}
