package com.example.chessserverspringboot.service;

import com.example.chessserverspringboot.websocket.GameSession;

public class GameTimer {

    private final GameSession game;
    private final int minutes;
    private final int increment;

    private int whiteTimeMs;
    private int blackTimeMs;

    private boolean whiteToMove = true;
    private boolean running = false;

    private Thread timerThread;

    public GameTimer(GameSession game, int minutes, int increment) {
        this.game = game;
        this.minutes = minutes;
        this.increment = increment;

        this.whiteTimeMs = minutes * 60 * 1000;
        this.blackTimeMs = minutes * 60 * 1000;
    }

    /* =====================================
                    GETTERS
       ===================================== */

    public int getMinutes() {
        return minutes;
    }

    public int getIncrement() {
        return increment;
    }

    public int getWhiteTimeMs() {
        return whiteTimeMs;
    }

    public int getBlackTimeMs() {
        return blackTimeMs;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    /* =====================================
                    TIMER
       ===================================== */

    public void start() {
        if (running) return;

        System.out.println(">>> TIMER START");

        running = true;

        timerThread = new Thread(() -> {
            System.out.println(">>> TIMER THREAD STARTED");

            try {
                while (running) {

                    Thread.sleep(500); // обновление каждые 500мс

                    if (whiteToMove) {
                        whiteTimeMs -= 500;
                        if (whiteTimeMs <= 0) {
                            running = false;
                            game.timeExpired("white");
                        }
                    } else {
                        blackTimeMs -= 500;
                        if (blackTimeMs <= 0) {
                            running = false;
                            game.timeExpired("black");
                        }
                    }

                    // отправляем обновлённое время игрокам
                    game.broadcastTimerUpdate(whiteTimeMs, blackTimeMs, whiteToMove);
                }

            } catch (Exception ignored) {}
        });

        timerThread.start();
    }

    public void switchTurn() {
        if (!running) return;

        // добавляем инкремент тому, кто сделал ход
        if (whiteToMove) {
            whiteTimeMs += increment * 1000;
        } else {
            blackTimeMs += increment * 1000;
        }

        whiteToMove = !whiteToMove;
    }

    public void stop() {
        running = false;

        if (timerThread != null && timerThread.isAlive()) {
            try {
                timerThread.join(100);
            } catch (Exception ignored) {}
        }
    }
}
