package com.example.chessserverspringboot.model;

import java.time.LocalDateTime;

public class MoveJson {
    public int moveNumber;
    public int playerId;
    public String color;
    public String piece;
    public String from;
    public String to;
    public boolean isCheck;
    public boolean isCheckmate;
    public LocalDateTime timestamp = LocalDateTime.now();
}
