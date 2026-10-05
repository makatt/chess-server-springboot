package com.example.chessserverspringboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end проверка API на реальной БД: регистрация -> логин -> статистика.
 * Ловит расхождения между JPA-сущностями и SQL-схемой (то, что не увидит
 * обычный @SpringBootTest без БД).
 */
class AuthFlowTests extends AbstractIntegrationTest {

    @Autowired
    private TestRestTemplate rest;

    @Test
    void registerLoginAndReadStats() {
        String username = "user_" + UUID.randomUUID().toString().substring(0, 8);
        String email = username + "@example.com";
        String password = "s3cret-password";

        // 1. Регистрация (создаёт строку в chess.users + chess.user_profile)
        ResponseEntity<String> registered = post("/api/auth/register", Map.of(
                "username", username,
                "email", email,
                "password", password));
        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2. Повторная регистрация того же логина должна быть отклонена
        ResponseEntity<String> duplicate = post("/api/auth/register", Map.of(
                "username", username,
                "email", email,
                "password", password));
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        // 3. Логин возвращает user_id
        ResponseEntity<Map> loggedIn = post("/api/auth/login", Map.of(
                "username", username,
                "password", password), Map.class);
        assertThat(loggedIn.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loggedIn.getBody()).containsKey("user_id");

        int userId = (int) loggedIn.getBody().get("user_id");

        // 4. Статистика игрока читается из chess.matches / chess.user_profile
        ResponseEntity<Map> stats = rest.getForEntity(
                "/api/matches/player/" + userId + "/stats", Map.class);
        assertThat(stats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stats.getBody()).containsEntry("total", 0);
        assertThat(stats.getBody()).containsEntry("rating", 1000);
    }

    @Test
    void actuatorHealthIsUp() {
        ResponseEntity<Map> health = rest.getForEntity("/actuator/health", Map.class);
        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(health.getBody()).containsEntry("status", "UP");
    }

    private ResponseEntity<String> post(String path, Map<String, String> body) {
        return post(path, body, String.class);
    }

    private <T> ResponseEntity<T> post(String path, Map<String, String> body, Class<T> type) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity(path, new HttpEntity<>(body, headers), type);
    }
}
