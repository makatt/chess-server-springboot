package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.Entity.Room;
import com.example.chessserverspringboot.Entity.RoomDTO;
import com.example.chessserverspringboot.Repository.RoomRepository;
import com.example.chessserverspringboot.websocket.GameSession;
import com.example.chessserverspringboot.websocket.Player;
import com.example.chessserverspringboot.websocket.RoomInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchmakerService {


    public synchronized GameSession getGameByRoomId(String roomId) {
        System.out.println("LOOKUP GAME room=" + roomId);
        return activeGames.get(roomId);
    }


    /** Все подключённые игроки */
    private final Map<String, Player> players = new HashMap<>();

    /** Активные игры по roomId */
    private final Map<String, GameSession> activeGames = new HashMap<>();

    /** Активные игры по имени игрока */
    private final Map<String, GameSession> gamesByPlayer = new HashMap<>();

    /** Комнаты в ожидании */
    private final Map<String, RoomInfo> waitingRooms = new HashMap<>();

    @Autowired
    private GameDatabaseService gameDB;

    @Autowired
    private RoomRepository roomRepo;

    /* ============================================================
                        ПОЛУЧЕНИЕ ИЛИ СОЗДАНИЕ ИГРОКА
       ============================================================ */
    public synchronized Player getOrCreatePlayer(String name, WebSocketSession session) {
        Player p = players.get(name);

        if (p == null) {
            p = new Player(name, session);
            players.put(name, p);
        } else {
            p.setSession(session);
        }

        return p;
    }

    /* ============================================================
                         СОЗДАНИЕ КОМНАТЫ
       ============================================================ */
    public synchronized RoomInfo createRoom(Player creator, int minutes, int increment) {

        String roomId = "room_" + UUID.randomUUID().toString().substring(0, 6);

        Room room = new Room();
        room.setRoomId(roomId);
        room.setCreator_id(Integer.parseInt(creator.getName()));
        room.setMinutes(minutes);
        room.setIncrement(increment);
        room.setStatus("waiting");
        roomRepo.save(room);

        RoomInfo info = new RoomInfo(roomId, creator, minutes, increment);

        waitingRooms.put(roomId, info);

        System.out.println("ROOM CREATED: " + roomId + " by user=" + creator.getName());

        return info;
    }

    /* ============================================================
                       СПИСОК КОМНАТ
       ============================================================ */
    public synchronized List<RoomDTO> getRooms() {
        List<RoomDTO> list = new ArrayList<>();

        for (RoomInfo room : waitingRooms.values()) {
            list.add(new RoomDTO(
                    room.getRoomId(),
                    Integer.parseInt(room.getCreator().getName()),
                    room.getMinutes(),
                    room.getIncrement(),
                    room.getStatus(),
                    room.getPlayersCount()
            ));
        }
        return list;
    }

    /* ============================================================
                         ПОДКЛЮЧЕНИЕ К КОМНАТЕ
       ============================================================ */
    public synchronized GameSession joinRoomById(String roomId, Player player) {

        RoomInfo info = waitingRooms.get(roomId);

        if (info == null) return null;

        // ❗ Запрет: создатель не может подключиться как второй игрок
        if (info.getCreator().getName().equals(player.getName())) {
            System.out.println("JOIN BLOCKED: creator cannot join own room");
            return null;
        }

        // ❗ Запрет: игрок уже в комнате
        for (Player p : info.getPlayers()) {
            if (p.getName().equals(player.getName())) {
                System.out.println("JOIN BLOCKED: player already in room");
                return null;
            }
        }

        if (!info.addPlayer(player)) {
            return null;
        }

        System.out.println("JOIN OK: user=" + player.getName() + " joined room " + roomId);

        // Ждем второго игрока
        if (!info.isFull()) return null;

        // Старт игры
        info.setStatus("playing");

        Player white = info.getPlayers().get(0);
        Player black = info.getPlayers().get(1);

        white.setColor("white");
        black.setColor("black");

        GameSession session = new GameSession(
                white,
                black,
                info.getMinutes(),
                info.getIncrement(),
                gameDB
        );

        int matchId = gameDB.createMatch(
                Integer.parseInt(white.getName()),
                Integer.parseInt(black.getName())
        );

        session.setMatchId(matchId);
        session.setRoomId(roomId);

        waitingRooms.remove(roomId);
        activeGames.put(roomId, session);

        gamesByPlayer.put(white.getName(), session);
        gamesByPlayer.put(black.getName(), session);

        System.out.println("GAME STARTED: " + roomId);

        return session;
    }

    /* ============================================================
                       ПОЛУЧИТЬ ИГРУ ПО ИГРОКУ
       ============================================================ */
    public synchronized GameSession getGameByPlayer(String playerName) {
        return gamesByPlayer.get(playerName);
    }

    /* ============================================================
                       ПОЛУЧИТЬ ИГРУ ПО ROOM ID
       ============================================================ */


    /* ============================================================
                       ОБРАБОТКА ДИСКОННЕКТА
       ============================================================ */
    public synchronized void handleDisconnect(String playerName) {
        Player p = players.get(playerName);
        if (p == null) return;

        GameSession game = gamesByPlayer.get(playerName);
        if (game != null) {
            game.onPlayerDisconnected(playerName);
        }
    }
}
