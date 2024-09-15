package ru.itis.kpfu.selyantsev.config;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
@TestPropertySource(locations = "classpath:application-test.yml")
public class ContainerConfiguration {

    private static final String DOCKER_IMAGE = "postgres:11.1";
    private static final String DATABASE_NAME = "tiny_url_test";
    private static final PostgreSQLContainer<?> postgresContainer;

    static {
        postgresContainer = new PostgreSQLContainer<>(DOCKER_IMAGE)
                .withReuse(true)
                .withDatabaseName(DATABASE_NAME);
        postgresContainer.start();
    }

    public static PostgreSQLContainer<?> getInstance() {
        return postgresContainer;
    }
}