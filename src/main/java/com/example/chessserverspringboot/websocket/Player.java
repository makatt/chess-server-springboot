package com.example.chessserverspringboot.websocket;

import org.springframework.web.socket.WebSocketSession;

public class Player {

    private final String name;
    private WebSocketSession session;
    private String color; // white / black

    public Player(String name, WebSocketSession session) {
        this.name = name;
        this.session = session;
    }

    public String getName() { return name; }
    public WebSocketSession getSession() { return session; }

    public void setSession(WebSocketSession session) {
        this.session = session;
    }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    // Безопасная отправка сообщений
    public synchronized void sendRaw(String json) {
        try {
            if (session != null && session.isOpen()) {
                session.sendMessage(new org.springframework.web.socket.TextMessage(json));
            }
        } catch (Exception e) {
            System.out.println("⚠ Ошибка отправки сообщения игроку " + name + ": " + e.getMessage());
        }
    }
    public boolean isOpen() {
        return session != null && session.isOpen();
    }

    public void send(String type, Object content) {
        String json = String.format("{\"type\":\"%s\",\"sender\":\"SERVER\",\"content\":\"%s\"}", type, content);
        sendRaw(json);
    }
}
