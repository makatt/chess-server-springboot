package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.websocket.GameSession;
import org.springframework.web.socket.TextMessage;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class GameTimer {
    private final Timer timer = new Timer(true);
    private long whiteTimeMs;
    private long blackTimeMs;
    private long lastMoveTime;
    private String activeColor;
    private final GameSession session;
    private final int incrementSec; // ⏱ приращение в секундах
    private boolean running = true;

    public GameTimer(GameSession session, int minutes, int incrementSec) {
        this.session = session;
        this.whiteTimeMs = minutes * 60L * 1000L;
        this.blackTimeMs = minutes * 60L * 1000L;
        this.incrementSec = incrementSec;
        this.activeColor = "white";
        this.lastMoveTime = System.currentTimeMillis();
        startTimerLoop();
    }

    public GameTimer(GameSession session, int minutes) {
        this.session = session;
        this.whiteTimeMs = minutes * 60L * 1000L;
        this.blackTimeMs = minutes * 60L * 1000L;
        this.activeColor = "white";
        this.incrementSec = 0;
        this.lastMoveTime = System.currentTimeMillis();
        startTimerLoop();
    }

    public synchronized void switchTurn() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastMoveTime;
        if (activeColor.equals("white")) {
            whiteTimeMs -= elapsed;
            whiteTimeMs += incrementSec * 1000L; // ➕ добавляем инкремент
        } else {
            blackTimeMs -= elapsed;
            blackTimeMs += incrementSec * 1000L;
        }
        activeColor = activeColor.equals("white") ? "black" : "white";
        lastMoveTime = now;
    }

    private void startTimerLoop() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (!running) return;
                long now = System.currentTimeMillis();
                long elapsed = now - lastMoveTime;
                if (activeColor.equals("white")) whiteTimeMs -= elapsed;
                else blackTimeMs -= elapsed;
                lastMoveTime = now;

                if (whiteTimeMs <= 0 || blackTimeMs <= 0) {
                    running = false;
                    String loser = (whiteTimeMs <= 0) ? "white" : "black";
                    session.timeExpired(loser);
                    cancel();
                    return;
                }

                try {
                    String timeMsg = String.format("white=%s, black=%s",
                            formatTime(whiteTimeMs), formatTime(blackTimeMs));
                    session.broadcast(new TextMessage(
                            "{\"type\":\"TIME_UPDATE\",\"sender\":\"SERVER\",\"content\":\"" + timeMsg + "\"}"
                    ));
                } catch (Exception ignored) {}
            }
        }, 1000, 1000);
    }

    public void stop() { running = false; timer.cancel(); }

    private String formatTime(long ms) {
        long seconds = ms / 1000;
        long minutes = seconds / 60;
        long sec = seconds % 60;
        DecimalFormat df = new DecimalFormat("00");
        return df.format(minutes) + ":" + df.format(sec);
    }
}
