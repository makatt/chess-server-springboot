package com.example.chessserverspringboot.model;

public class Piece {
    private String type;  // "pawn", "rook", "knight", "bishop", "queen", "king"
    private String color; // "white" или "black"

    public Piece() {}

    public Piece(String type, String color) {
        this.type = type;
        this.color = color;
    }

    public String getType() { return type; }
    public String getColor() { return color; }
}
