package com.example.chessserverspringboot.websocket;

import java.util.ArrayList;
import java.util.List;

public class RoomInfo {

    private final String roomId;
    private final int minutes;
    private final int increment;

    private final List<Player> players = new ArrayList<>(2);

    private String status = "waiting"; // waiting | playing

    public RoomInfo(String roomId, Player creator, int minutes, int increment) {
        this.roomId = roomId;
        this.minutes = minutes;
        this.increment = increment;
        this.players.add(creator);
    }

    /* ===================== GETTERS ===================== */

    public String getRoomId() {
        return roomId;
    }

    public int getMinutes() {
        return minutes;
    }

    public int getIncrement() {
        return increment;
    }

    public String getStatus() {
        return status;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getCreator() {
        return players.get(0);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    /* ===================== LOGIC ===================== */

    public boolean addPlayer(Player p) {
        if (players.size() >= 2) return false;

        // 🔥 запрещаем повторного игрока
        for (Player existing : players) {
            if (existing.getName().equals(p.getName())) {
                return false;
            }
        }

        players.add(p);
        return true;
    }

    public boolean isFull() {
        return players.size() == 2;
    }

    public int getPlayersCount() {
        return players.size();
    }
}
