package com.example.chessserverspringboot.websocket;

public class RoomInfo {
    private String time;   // например "3|2"
    private int players;   // сколько ждёт

    public RoomInfo(String time, int players) {
        this.time = time;
        this.players = players;
    }

    public String getTime() { return time; }
    public int getPlayers() { return players; }
}
