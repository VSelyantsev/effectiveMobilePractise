package ru.itis.kpfu.selyantsev.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.itis.kpfu.selyantsev.model.Url;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UrlRepository extends JpaRepository<Url, UUID> {
    Optional<Url> findByShortUrl(String shortUrl);

    boolean existsByShortUrl(String alias);

    @Modifying
    @Transactional
    @Query("DELETE FROM t_url u WHERE u.expirationDate < :now")
    void deleteByExpirationDate(@Param(value = "now") LocalDateTime now);
}
