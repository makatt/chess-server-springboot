package com.example.chessserverspringboot.websocket;

import com.example.chessserverspringboot.model.GameState;

public class GameSession {
    private final Player white;
    private final Player black;
    private final GameState state;

    public GameSession(Player white, Player black) {
        this.white = white;
        this.black = black;
        this.state = new GameState();
    }

    public Player getWhite() { return white; }
    public Player getBlack() { return black; }
    public GameState getState() { return state; }

    public Player getOpponent(Player player) {
        return player.getName().equals(white.getName()) ? black : white;
    }
}
