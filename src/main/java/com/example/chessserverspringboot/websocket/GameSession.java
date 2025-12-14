package com.example.chessserverspringboot.websocket;

import com.example.chessserverspringboot.service.GameDatabaseService;
import com.example.chessserverspringboot.service.GameTimer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.bhlangonijr.chesslib.Side;
import com.github.bhlangonijr.chesslib.Square;
import org.springframework.web.socket.TextMessage;
import com.github.bhlangonijr.chesslib.Board;
import com.github.bhlangonijr.chesslib.move.Move;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameSession {

    private final Player white;
    private final Player black;

    private final int minutes;
    private final int increment;

    private int matchId;
    private String roomId;

    private final AtomicBoolean finished = new AtomicBoolean(false);
    private final ObjectMapper mapper = new ObjectMapper();

    private final GameTimer timer;

    private int moveNumber = 1;

    private final GameDatabaseService gameDB;

    /** начальная FEN */
    private final Board board = new Board();


    /* ============================================================
                           CONSTRUCTOR
       ============================================================ */
    public GameSession(Player white, Player black, int minutes, int increment, GameDatabaseService gameDB) {
        this.white = white;
        this.black = black;
        this.minutes = minutes;
        this.increment = increment;
        this.gameDB = gameDB;

        this.timer = new GameTimer(this, minutes, increment);

        sendStart();
        sendUpdate(board.getFen());


        timer.start();
    }

    /* ============================================================
                               START
       ============================================================ */
    private void sendStart() {
        String msg = String.format(
                "white=%s, black=%s, time=%d|%d",
                white.getName(),
                black.getName(),
                minutes,
                increment
        );

        safeSend(white, new MessageModel("START", "SERVER", msg));
        safeSend(black, new MessageModel("START", "SERVER", msg));
    }

    /* ============================================================
                             HELPERS
       ============================================================ */

    public void setRoomId(String id) { this.roomId = id; }

    public void setMatchId(int id) { this.matchId = id; }

    public Player getWhite() { return white; }
    public Player getBlack() { return black; }

    /* ============================================================
                             SENDING
       ============================================================ */

    private void safeSend(Player p, MessageModel msg) {
        try {
            if (p != null && p.getSession() != null && p.getSession().isOpen()) {
                p.getSession().sendMessage(new TextMessage(mapper.writeValueAsString(msg)));
            }
        } catch (IOException ignored) {}
    }

    private void safeBroadcast(MessageModel msg) {
        safeSend(white, msg);
        safeSend(black, msg);
    }

    /* ============================================================
                              UPDATE
       ============================================================ */

    public void sendUpdate(String fen) {
        safeBroadcast(new MessageModel("UPDATE", "SERVER", fen));

    }

    public void broadcastTimerUpdate(int w, int b, boolean whiteTurn) {
        if (finished.get()) return;

        String json = String.format(
                "{\"white\":%d, \"black\":%d, \"turn\":\"%s\"}",
                w, b, whiteTurn ? "white" : "black"
        );

        safeBroadcast(new MessageModel("TIMER", "SERVER", json));
    }

    /* ============================================================
                                 MOVE
       ============================================================ */
    public synchronized void handleMove(Player player, String moveStr) {

        if (finished.get()) return;

        boolean whiteTurn = board.getSideToMove() == Side.WHITE;


        if (whiteTurn && player != white) return;
        if (!whiteTurn && player != black) return;

        try {
            Move move = new Move(
                    Square.fromValue(moveStr.substring(0, 2).toUpperCase()),
                    Square.fromValue(moveStr.substring(2, 4).toUpperCase())
            );


            if (!board.isMoveLegal(move, true)) {
                safeSend(player, new MessageModel(
                        "ERROR",
                        "SERVER",
                        "ILLEGAL_MOVE"
                ));
                return;
            }

            board.doMove(move);
            timer.switchTurn();

            gameDB.saveMove(
                    matchId,
                    Integer.parseInt(player.getName()),
                    moveNumber++,
                    moveStr,
                    board.getFen()
            );

            sendUpdate(board.getFen());
            System.out.println("MOVE RECEIVED: " + move);

            // 🏁 конец игры
            if (board.isMated() || board.isDraw()) {
                finished.set(true);
                timer.stop();

                safeBroadcast(new MessageModel(
                        "GAME_OVER",
                        "SERVER",
                        board.isMated() ? "checkmate" : "draw"
                ));
            }

        } catch (Exception e) {
            e.printStackTrace();
            safeSend(player, new MessageModel(
                    "ERROR",
                    "SERVER",
                    "MOVE_FAILED"
            ));
        }
    }



    private String applyMoveDummy(String fen) {
        if (fen.contains(" w "))
            return fen.replace(" w ", " b ");
        return fen.replace(" b ", " w ");
    }

    /* ============================================================
                           DISCONNECT
       ============================================================ */

    public void onPlayerDisconnected(String name) {
        if (finished.get()) return;

        finished.set(true);
        timer.stop();

        String winner = name.equals(white.getName()) ? black.getName() : white.getName();

        gameDB.finishMatch(
                matchId,
                Integer.parseInt(winner),
                board.getFen(),
                "disconnect"
        );


        safeBroadcast(new MessageModel("FINISH", "SERVER",
                "Игрок " + name + " отключился"));
    }

    /* ============================================================
                              TIMEOUT
       ============================================================ */

    public void timeExpired(String color) {
        if (finished.get()) return;

        finished.set(true);
        timer.stop();

        String winner = color.equals("white") ? black.getName() : white.getName();

        gameDB.finishMatch(
                matchId,
                Integer.parseInt(winner),
                board.getFen(),
                "time"
        );


        safeBroadcast(new MessageModel("FINISH", "SERVER",
                "Время игрока " + color + " закончилось"));
    }
}
