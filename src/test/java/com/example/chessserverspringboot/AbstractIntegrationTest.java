package com.example.chessserverspringboot;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.MountableFile;

/**
 * База для интеграционных тестов: реальный PostgreSQL 16 в контейнере.
 *
 * Схема БД берётся из того же файла, что применяется в docker-compose
 * (docker/postgres/init/01_schema.sql) — тесты проверяют именно ту схему,
 * которая поедет в прод, а не отдельную «тестовую» копию.
 *
 * Контейнер один на весь запуск JVM (singleton pattern) — тесты не тратят
 * время на повторный старт postgres.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withCopyFileToContainer(
                            MountableFile.forHostPath("docker/postgres/init/01_schema.sql"),
                            "/docker-entrypoint-initdb.d/01_schema.sql");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }
}
