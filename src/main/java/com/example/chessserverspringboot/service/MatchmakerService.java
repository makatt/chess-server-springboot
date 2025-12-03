package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.websocket.GameSession;
import com.example.chessserverspringboot.websocket.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchmakerService {

    private final Map<String, Queue<Player>> waitingRooms = new HashMap<>();
    private final Map<String, GameSession> activeGames = new HashMap<>();

    @Autowired
    private GameDatabaseService gameDB;

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

            //    конструктор из GameSession
            GameSession session = new GameSession(
                    opponent,
                    player,
                    minutes,
                    increment,
                    gameDB
            );

            int matchId = gameDB.createMatch(
                    Integer.parseInt(opponent.getName()),
                    Integer.parseInt(player.getName())
            );
            session.setMatchId(matchId);

            activeGames.put(opponent.getName(), session);
            activeGames.put(player.getName(), session);

            return session;

        } else {
            createRoom(player, minutes, increment);
            return null;
        }
    }


    /** 🔥 Получение списка доступных комнат */
    public synchronized Map<String, Integer> getAvailableRooms() {
        Map<String, Integer> summary = new LinkedHashMap<>();
        for (var entry : waitingRooms.entrySet()) {
            summary.put(entry.getKey(), entry.getValue().size());
        }
        return summary;
    }

    /** 🔥 Получение активной игры по ID игрока */
    public GameSession getGameByPlayer(String playerName) {
        return activeGames.get(playerName);
    }
}
