package com.example.chessserverspringboot.model;

public class Position {
    private int row; // 0-7
    private int col; // 0-7

    public Position() {}

    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() { return row; }
    public int getCol() { return col; }

    public static Position fromAlgebraic(String notation) {
        // Пример: "e2" -> row=6, col=4
        int col = notation.charAt(0) - 'a';
        int row = 8 - Character.getNumericValue(notation.charAt(1));
        return new Position(row, col);
    }

    public String toAlgebraic() {
        return "" + (char) ('a' + col) + (8 - row);
    }
}
