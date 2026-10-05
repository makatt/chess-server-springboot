-- =============================================================================
--  Схема БД chess-сервера.
--  Единственный источник истины о структуре — JPA-сущности в
--  src/main/java/com/example/chessserverspringboot/{Entity,Registration}/.
--  Файл применяется автоматически при первой инициализации volume db_data
--  (postgres выполняет /docker-entrypoint-initdb.d/*.sql) и в интеграционных
--  тестах через Testcontainers — то есть одна и та же схема проверяется в CI.
-- =============================================================================

CREATE SCHEMA IF NOT EXISTS chess;

CREATE TABLE IF NOT EXISTS chess.users (
    user_id       SERIAL PRIMARY KEY,
    username      VARCHAR(255) NOT NULL UNIQUE,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    created_at    TIMESTAMP,
    last_login    TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chess.user_profile (
    user_id      INTEGER PRIMARY KEY REFERENCES chess.users (user_id) ON DELETE CASCADE,
    avatar_url   VARCHAR(255),
    bio          TEXT,
    rating       INTEGER NOT NULL DEFAULT 1000,
    games_played INTEGER NOT NULL DEFAULT 0,
    games_won    INTEGER NOT NULL DEFAULT 0,
    games_lost   INTEGER NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS chess.rooms (
    room_id    VARCHAR(255) PRIMARY KEY,
    creator_id INTEGER,
    minutes    INTEGER,
    increment  INTEGER,
    status     VARCHAR(255) NOT NULL DEFAULT 'waiting',
    created_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chess.matches (
    match_id   SERIAL PRIMARY KEY,
    player1_id INTEGER,
    player2_id INTEGER,
    winner_id  INTEGER,
    started_at TIMESTAMP,
    ended_at   TIMESTAMP,
    final_fen  TEXT,
    end_reason VARCHAR(255)
);

-- запросы статистики игрока ищут матчи по обоим полям игрока
CREATE INDEX IF NOT EXISTS idx_matches_player1 ON chess.matches (player1_id);
CREATE INDEX IF NOT EXISTS idx_matches_player2 ON chess.matches (player2_id);
CREATE INDEX IF NOT EXISTS idx_matches_winner  ON chess.matches (winner_id);

CREATE TABLE IF NOT EXISTS chess.match_moves (
    move_id      SERIAL PRIMARY KEY,
    match_id     INTEGER REFERENCES chess.matches (match_id) ON DELETE CASCADE,
    player_id    INTEGER,
    move_number  INTEGER,
    move_data    JSONB,
    fen_position TEXT,
    created_at   TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_match_moves_match ON chess.match_moves (match_id);
