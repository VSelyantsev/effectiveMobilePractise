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
import ru.itis.kpfu.selyantsev.exceptions.UserNotFoundException;
import ru.itis.kpfu.selyantsev.model.User;
import ru.itis.kpfu.selyantsev.repository.CrudRepository;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import static reactor.core.scheduler.Schedulers.fromExecutor;

@Repository
@RequiredArgsConstructor
@SuppressWarnings("BlockingMethodInNonBlockingContext")
public class UserRepositoryImpl implements CrudRepository<User, UUID> {

    private final SessionFactory sessionFactory;
    private final ExecutorService blockingPool = Executors.newFixedThreadPool(5);

    @Override
    public Mono<UUID> create(User user) {
        return Mono.fromCallable(() -> {
            sessionFactory.inTransaction(session -> session.persist(user));
            return user.getUserId();
        })
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()));
    }

    @Override
    public Mono<User> findById(UUID uuid) {
        return Mono.fromCallable(() -> {
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            User userFromDb = session.find(User.class, uuid);
            tx.commit();
            return userFromDb;
        })
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorMap(IllegalArgumentException.class, ex -> new UserNotFoundException(uuid))
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()));
    }

    @Override
    public Flux<User> findAll(int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return Mono.fromCallable(() -> {
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            List<User> userList = session.createQuery(
                    "FROM t_user u LEFT JOIN FETCH u.taskList", User.class
                    ).setFirstResult(offset)
                    .setMaxResults(pageSize)
                    .list();
            tx.commit();
            return userList;
        })
        .subscribeOn(fromExecutor(blockingPool))
        .flatMapMany(Flux::fromIterable)
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()));
    }

    @Override
    public Mono<User> update(User user) {
        return Mono.fromCallable(() -> {
            Session session = sessionFactory.openSession();
            Transaction tx = session.beginTransaction();
            User updatedUser = session.merge(user);
            tx.commit();
            return updatedUser;
        })
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()));
    }

    @Override
    public Mono<Void> deleteById(UUID uuid) {
        return Mono.fromRunnable(() -> sessionFactory.inTransaction(session -> {
            User userToDelete = session.find(User.class, uuid);
            session.remove(userToDelete);
        }))
        .subscribeOn(fromExecutor(blockingPool))
        .onErrorMap(IllegalArgumentException.class, ex -> new UserNotFoundException(uuid))
        .onErrorMap(HibernateException.class, e -> new DatabaseException(e.getCause()))
        .then();
    }
}
