package com.example.chessserverspringboot.websocket;


public class MessageModel {
    private String type;   // тип сообщения: CONNECT, START, MOVE, END, CHAT
    private String sender; // id или имя игрока
    private String content; // содержимое (например ход, сообщение, состояние доски)
    private String roomId;
    public MessageModel() {}

    public MessageModel(String type, String sender, String content) {
        this.type = type;
        this.sender = sender;
        this.content = content;
    }

    public String getRoomId() {
        return roomId;
    }

    public enum MessageType {
        CONNECT,   // подключение игрока
        START,     // начало партии
        MOVE,      // ход фигуры
        END,       // конец игры
        CHAT,      // сообщение в чате
        ERROR      // ошибки
    }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
