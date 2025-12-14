package com.example.chessserverspringboot.Entity;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rooms", schema = "chess")
public class Room {

    @Id
    @Column(name = "room_id")
    private String roomId;

    private Integer creator_id;
    private Integer minutes;
    private Integer increment;

    private String status = "waiting";

    private LocalDateTime created_at = LocalDateTime.now();

    public String getRoomId() { return roomId; }
    public Integer getCreator_id() { return creator_id; }
    public Integer getMinutes() { return minutes; }
    public Integer getIncrement() { return increment; }
    public String getStatus() { return status; }

    public void setRoomId(String id) { this.roomId = id; }
    public void setCreator_id(Integer id) { this.creator_id = id; }
    public void setMinutes(Integer m) { this.minutes = m; }
    public void setIncrement(Integer i) { this.increment = i; }
    public void setStatus(String s) { this.status = s; }

}
