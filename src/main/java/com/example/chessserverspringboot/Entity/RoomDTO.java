package com.example.chessserverspringboot.Entity;

public class RoomDTO {

    public String roomId;
    public int creatorId;
    public int minutes;
    public int increment;
    public String status;
    public int playersCount;

    public RoomDTO(String roomId, int creatorId, int minutes, int increment, String status, int playersCount) {
        this.roomId = roomId;
        this.creatorId = creatorId;
        this.minutes = minutes;
        this.increment = increment;
        this.status = status;
        this.playersCount = playersCount;
    }
}
