package com.example.chessserverspringboot.websocket;

import org.springframework.web.socket.WebSocketSession;

public class Player {
    private final String name;
    private final WebSocketSession session;
    private String color; // "white" или "black"

    public Player(String name, WebSocketSession session) {
        this.name = name;
        this.session = session;
    }

    public String getName() { return name; }
    public WebSocketSession getSession() { return session; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
}
