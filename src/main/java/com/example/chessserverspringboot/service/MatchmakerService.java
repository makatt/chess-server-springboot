package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.websocket.GameSession;
import com.example.chessserverspringboot.websocket.Player;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchmakerService {

    // Очередь ожидания по ключу времени: "3|2", "5|0", "10|0"
    private final Map<String, Queue<Player>> waitingRooms = new HashMap<>();

    // Активные игры
    private final Map<String, GameSession> activeGames = new HashMap<>();

    /** Создание комнаты */
    public synchronized void createRoom(Player player, int minutes, int increment) {
        String key = minutes + "|" + increment;
        waitingRooms.putIfAbsent(key, new LinkedList<>());
        waitingRooms.get(key).add(player);
    }

    /** Присоединение к комнате */
    public synchronized GameSession joinRoom(Player player, int minutes, int increment) {
        String key = minutes + "|" + increment;
        Queue<Player> queue = waitingRooms.get(key);

        if (queue != null && !queue.isEmpty()) {
            Player opponent = queue.poll();

            opponent.setColor("white");
            player.setColor("black");

            GameSession session = new GameSession(opponent, player, minutes, increment);
            activeGames.put(opponent.getName(), session);
            activeGames.put(player.getName(), session);
            return session;
        } else {
            // если никого нет — создать новую комнату
            createRoom(player, minutes, increment);
            return null;
        }
    }

    /** Получение списка комнат */
    public synchronized Map<String, Integer> getAvailableRooms() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        for (var entry : waitingRooms.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().size());
        }
        return summary;
    }

    public GameSession getGameByPlayer(String playerName) {
        return activeGames.get(playerName);
    }

    public void removePlayer(String playerName) {
        activeGames.remove(playerName);
        for (Queue<Player> q : waitingRooms.values()) {
            q.removeIf(p -> p.getName().equals(playerName));
        }
    }
}
