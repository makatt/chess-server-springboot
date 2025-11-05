package com.example.chessserverspringboot.websocket;

import com.example.chessserverspringboot.model.GameState;
import com.example.chessserverspringboot.service.GameTimer;
import org.springframework.web.socket.TextMessage;

public class GameSession {
    private final Player white;
    private final Player black;
    private final GameState state;
    private GameTimer timer;

    // Конструктор с increment
    public GameSession(Player white, Player black, int minutes, int incrementSec) {
        this.white = white;
        this.black = black;
        this.state = new GameState();
        this.timer = new GameTimer(this, minutes, incrementSec);
    }

    // ✅ Старый конструктор (без времени)
    public GameSession(Player white, Player black) {
        this.white = white;
        this.black = black;
        this.state = new GameState();
    }

    // ✅ Новый конструктор (с временем)
    public GameSession(Player white, Player black, int minutes) {
        this.white = white;
        this.black = black;
        this.state = new GameState();
        this.timer = new GameTimer(this, minutes);
    }

    public Player getWhite() { return white; }
    public Player getBlack() { return black; }
    public GameState getState() { return state; }
    public GameTimer getTimer() { return timer; }

    public Player getOpponent(Player player) {
        return player.getName().equals(white.getName()) ? black : white;
    }

    // Когда время истекло — уведомляем обоих игроков
    public void timeExpired(String loserColor) {
        String winner = loserColor.equals("white") ? black.getName() : white.getName();
        try {
            white.getSession().sendMessage(new TextMessage(
                    "{\"type\":\"TIMEOUT\",\"sender\":\"SERVER\",\"content\":\"" + winner + " выиграл по времени\"}"
            ));
            black.getSession().sendMessage(new TextMessage(
                    "{\"type\":\"TIMEOUT\",\"sender\":\"SERVER\",\"content\":\"" + winner + " выиграл по времени\"}"
            ));
        } catch (Exception ignored) {}
        if (timer != null) timer.stop();
    }
    public void broadcast(TextMessage msg) {
        try {
            white.getSession().sendMessage(msg);
            black.getSession().sendMessage(msg);
        } catch (Exception ignored) {}
    }

}
