package com.example.chessserverspringboot.websocket;

import com.example.chessserverspringboot.model.GameState;
import com.example.chessserverspringboot.service.GameDatabaseService;
import com.example.chessserverspringboot.service.GameTimer;
import org.springframework.web.socket.TextMessage;

public class GameSession {

    private final Player white;
    private final Player black;
    private final GameState state;
    private final GameTimer timer;

    private final GameDatabaseService gameDB;

    private int matchId;

    // ---- set/get matchId ----
    public void setMatchId(int id) { this.matchId = id; }
    public int getMatchId() { return matchId; }

    // ---- constructor ----
    public GameSession(Player white,
                       Player black,
                       int minutes,
                       int increment,
                       GameDatabaseService gameDB) {

        this.white = white;
        this.black = black;
        this.state = new GameState();
        this.timer = new GameTimer(this, minutes, increment);

        this.gameDB = gameDB;
    }

    public Player getWhite() { return white; }
    public Player getBlack() { return black; }
    public GameState getState() { return state; }
    public GameTimer getTimer() { return timer; }

    public Player getOpponent(Player player) {
        return player.getName().equals(white.getName()) ? black : white;
    }

    // ---- send message to both players ----
    public void broadcast(TextMessage msg) {
        try {
            white.getSession().sendMessage(msg);
            black.getSession().sendMessage(msg);
        } catch (Exception ignored) {}
    }

    // ---- handle timeout ----
    public void timeExpired(String loserColor) {

        // winner = opposite color
        Player winnerPlayer = loserColor.equals("white") ? black : white;

        int winnerId = Integer.parseInt(winnerPlayer.getName());
        String finalFen = state.toFEN();

        // === SAVE MATCH IN DB ===
        gameDB.finishMatch(
                matchId,
                winnerId,
                finalFen,
                "timeout"
        );

        try {
            broadcast(new TextMessage(
                    "{\"type\":\"TIMEOUT\",\"sender\":\"SERVER\",\"content\":\"" +
                            winnerPlayer.getName() + " выиграл по времени\"}"
            ));
        } catch (Exception ignored) {}

        timer.stop();
    }
}
