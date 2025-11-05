package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.websocket.GameSession;
import com.example.chessserverspringboot.websocket.Player;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchmakerService {

    private final Queue<Player> waitingPlayers = new LinkedList<>();
    private final Map<String, GameSession> activeGames = new HashMap<>();

    // Добавляем игрока, создаем сессию если возможно
    public synchronized GameSession addPlayer(Player player) {
        if (waitingPlayers.isEmpty()) {
            waitingPlayers.add(player);
            return null; // ждём второго
        } else {
            Player opponent = waitingPlayers.poll();

            // Назначаем цвета
            opponent.setColor("white");
            player.setColor("black");

            GameSession session = new GameSession(opponent, player);
            activeGames.put(opponent.getName(), session);
            activeGames.put(player.getName(), session);

            return session;
        }
    }

    public GameSession getGameByPlayer(String playerName) {
        return activeGames.get(playerName);
    }

    public void removePlayer(String playerName) {
        waitingPlayers.removeIf(p -> p.getName().equals(playerName));
        activeGames.remove(playerName);
    }
}
