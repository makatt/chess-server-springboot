package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.websocket.GameSession;
import com.example.chessserverspringboot.websocket.Player;
import com.example.chessserverspringboot.websocket.RoomInfo;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MatchmakerService {

    // Очереди игроков по времени партии (например, 3 мин, 10 мин)
    private final Map<Integer, Queue<Player>> waitingRooms = new HashMap<>();

    // Активные игры
    private final Map<String, GameSession> activeGames = new HashMap<>();

    // === Прежняя логика (без выбора времени, общий поиск) ===
    private final Queue<Player> waitingPlayers = new LinkedList<>();

    public synchronized GameSession addPlayer(Player player) {
        if (waitingPlayers.isEmpty()) {
            waitingPlayers.add(player);
            return null;
        } else {
            Player opponent = waitingPlayers.poll();
            opponent.setColor("white");
            player.setColor("black");
            GameSession session = new GameSession(opponent, player);
            activeGames.put(opponent.getName(), session);
            activeGames.put(player.getName(), session);
            return session;
        }
    }

    // === Новая логика с комнатами ===

    /** Создание комнаты с определённым временем (в минутах) */
    public synchronized void createRoom(Player player, int minutes) {
        waitingRooms.putIfAbsent(minutes, new LinkedList<>());
        waitingRooms.get(minutes).add(player);
    }

    /** Попытка присоединиться к существующей комнате по времени */
    public synchronized GameSession joinRoom(Player player, int minutes) {
        Queue<Player> queue = waitingRooms.get(minutes);
        if (queue != null && !queue.isEmpty()) {
            Player opponent = queue.poll();

            // Назначаем цвета
            opponent.setColor("white");
            player.setColor("black");

            GameSession session = new GameSession(opponent, player, minutes);
            activeGames.put(opponent.getName(), session);
            activeGames.put(player.getName(), session);
            return session;
        } else {
            createRoom(player, minutes); // если комнаты нет — создать новую
            return null;
        }
    }

    /** Получить список всех доступных комнат (по минутам) */
    public synchronized List<RoomInfo> getAvailableRooms() {
        List<RoomInfo> list = new ArrayList<>();
        for (var entry : waitingRooms.entrySet()) {
            int minutes = entry.getKey();
            int players = entry.getValue().size();
            list.add(new RoomInfo(minutes + "|0", players)); // пока без инкремента
        }
        // можно отсортировать по времени
        list.sort(Comparator.comparing(RoomInfo::getTime));
        return list;
    }


    // === Общие методы ===
    public GameSession getGameByPlayer(String playerName) {
        return activeGames.get(playerName);
    }

    public void removePlayer(String playerName) {
        waitingPlayers.removeIf(p -> p.getName().equals(playerName));
        activeGames.remove(playerName);
        for (Queue<Player> q : waitingRooms.values()) {
            q.removeIf(p -> p.getName().equals(playerName));
        }
    }



}
