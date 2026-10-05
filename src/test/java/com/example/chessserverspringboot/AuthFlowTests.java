package com.example.chessserverspringboot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
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

    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
            new ParameterizedTypeReference<>() {
            };

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
        ResponseEntity<Map<String, Object>> loggedIn = postJson("/api/auth/login", Map.of(
                "username", username,
                "password", password));
        assertThat(loggedIn.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loggedIn.getBody()).containsKey("user_id");

        int userId = ((Number) loggedIn.getBody().get("user_id")).intValue();

        // 4. Статистика игрока читается из chess.matches / chess.user_profile
        ResponseEntity<Map<String, Object>> stats = rest.exchange(
                "/api/matches/player/" + userId + "/stats", HttpMethod.GET, null, JSON_OBJECT);
        assertThat(stats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stats.getBody()).containsEntry("total", 0);
        assertThat(stats.getBody()).containsEntry("rating", 1000);
    }

    @Test
    void actuatorHealthIsUp() {
        ResponseEntity<Map<String, Object>> health = rest.exchange(
                "/actuator/health", HttpMethod.GET, null, JSON_OBJECT);
        assertThat(health.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(health.getBody()).containsEntry("status", "UP");
    }

    private ResponseEntity<String> post(String path, Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.postForEntity(path, new HttpEntity<>(body, headers), String.class);
    }

    private ResponseEntity<Map<String, Object>> postJson(String path, Map<String, String> body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), JSON_OBJECT);
    }
}
