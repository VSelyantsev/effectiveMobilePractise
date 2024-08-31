package ru.itis.kpfu.selyantsev.repository.impl;

import lombok.RequiredArgsConstructor;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.itis.kpfu.selyantsev.exceptions.DatabaseException;
import ru.itis.kpfu.selyantsev.model.Task;
import ru.itis.kpfu.selyantsev.repository.CrudRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static reactor.core.scheduler.Schedulers.fromExecutor;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class TaskRepositoryImpl implements CrudRepository<Task, UUID> {

    private final SessionFactory sessionFactory;
    private static final ExecutorService blockingPool = Executors.newFixedThreadPool(5);

    @Override
    public Mono<UUID> create(Task task) {
        return Mono.fromCallable(() -> {
            sessionFactory.inTransaction(session -> session.persist(task));
            return task.getTaskId();
        })
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()));
    }

    @Override
    public Mono<Task> findById(UUID uuid) {
        return Mono.fromCallable(() -> {
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            Task taskFromDb = session.find(Task.class, uuid);
            tx.commit();
            return taskFromDb;
        })
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(IllegalArgumentException.class, ex -> Mono.empty())
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()));
    }

    @Override
    public Flux<Task> findAll(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return Mono.fromCallable(() -> {
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            List<Task> tasks = session.createQuery(
                    "FROM t_task", Task.class
                    ).setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .list();
            tx.commit();
            return tasks;
        })
        .subscribeOn(fromExecutor(blockingPool))
        .flatMapMany(Flux::fromIterable)
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()));
    }

    @Override
    public Mono<Task> update(Task task) {
        return Mono.fromCallable(() -> {
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            Task updateUser = session.merge(task);
            tx.commit();
            return updateUser;
        })
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()));
    }

    @Override
    public Mono<Void> deleteById(UUID uuid) {
        return Mono.fromRunnable(() -> sessionFactory.inTransaction(session -> {
            Task taskToDelete = session.find(Task.class, uuid);
            session.remove(taskToDelete);
        }))
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorResume(IllegalArgumentException.class, ex -> Mono.empty())
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()))
        .then();
    }
}
