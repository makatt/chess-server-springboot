package com.example.chessserverspringboot.websocket;

import com.example.chessserverspringboot.service.MatchmakerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChessWebSocketHandler extends TextWebSocketHandler {

    private final MatchmakerService matchmaker;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public ChessWebSocketHandler(MatchmakerService matchmaker) {
        this.matchmaker = matchmaker;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println("✅ Игрок подключился: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        MessageModel msg = mapper.readValue(message.getPayload(), MessageModel.class);

        switch (msg.getType()) {
            case "CONNECT" -> {
                Player player = new Player(msg.getSender(), session);
                GameSession game = matchmaker.addPlayer(player);

                if (game == null) {
                    session.sendMessage(new TextMessage(
                            mapper.writeValueAsString(new MessageModel("WAIT", "SERVER", "Ожидание второго игрока..."))
                    ));
                } else {
                    // Оповещаем обоих игроков
                    String startInfo = String.format("white=%s, black=%s",
                            game.getWhite().getName(), game.getBlack().getName());

                    game.getWhite().getSession().sendMessage(new TextMessage(
                            mapper.writeValueAsString(new MessageModel("START", "SERVER", startInfo))
                    ));
                    game.getBlack().getSession().sendMessage(new TextMessage(
                            mapper.writeValueAsString(new MessageModel("START", "SERVER", startInfo))
                    ));
                }
            }

            case "MOVE" -> {
                GameSession game = matchmaker.getGameByPlayer(msg.getSender());
                if (game != null) {
                    String[] move = msg.getContent().split("-");
                    String result = game.getState().makeMove(move[0], move[1]);

                    if (result.equals("OK") || result.equals("CHECK")) {
                        // переключаем таймер
                        if (game.getTimer() != null) game.getTimer().switchTurn();

                        // обновляем доску
                        String newState = mapper.writeValueAsString(game.getState());
                        broadcastToGame(game, new MessageModel("UPDATE", "SERVER", newState));

                        if (result.equals("CHECK")) {
                            broadcastToGame(game, new MessageModel("CHECK", "SERVER", "Король под шахом!"));
                        }
                    } else if (result.equals("CHECKMATE")) {
                        broadcastToGame(game, new MessageModel("CHECKMATE", "SERVER", "Мат! Игра окончена."));
                        if (game.getTimer() != null) game.getTimer().stop();
                    } else {
                        session.sendMessage(new TextMessage(mapper.writeValueAsString(
                                new MessageModel("ERROR", "SERVER", "Неверный ход или не ваш ход"))));
                    }
                }
            }

            case "CREATE_ROOM" -> {
                int minutes = Integer.parseInt(msg.getContent());
                Player player = new Player(msg.getSender(), session);
                matchmaker.createRoom(player, minutes);

                session.sendMessage(new TextMessage(mapper.writeValueAsString(
                        new MessageModel("WAIT", "SERVER",
                                "Комната создана (" + minutes + " мин). Ожидание соперника..."))
                ));
            }

            case "JOIN_ROOM" -> {
                int minutes = Integer.parseInt(msg.getContent());
                Player player = new Player(msg.getSender(), session);
                var game = matchmaker.joinRoom(player, minutes);

                if (game == null) {
                    session.sendMessage(new TextMessage(mapper.writeValueAsString(
                            new MessageModel("WAIT", "SERVER",
                                    "Ожидание комнаты с " + minutes + " мин..."))
                    ));
                } else {
                    String startInfo = String.format("white=%s, black=%s, time=%d мин",
                            game.getWhite().getName(), game.getBlack().getName(), minutes);

                    game.getWhite().getSession().sendMessage(new TextMessage(mapper.writeValueAsString(
                            new MessageModel("START", "SERVER", startInfo))
                    ));
                    game.getBlack().getSession().sendMessage(new TextMessage(mapper.writeValueAsString(
                            new MessageModel("START", "SERVER", startInfo))
                    ));
                }
            }

            case "ROOMS" -> {
                var rooms = matchmaker.getAvailableRooms();
                String json = mapper.writeValueAsString(rooms);
                session.sendMessage(new TextMessage(mapper.writeValueAsString(
                        new MessageModel("ROOMS", "SERVER", json))
                ));
            }




            default -> session.sendMessage(new TextMessage("❌ Неизвестный тип сообщения"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    private void broadcastToGame(GameSession game, MessageModel msg) throws Exception {
        String json = mapper.writeValueAsString(msg);
        game.getWhite().getSession().sendMessage(new TextMessage(json));
        game.getBlack().getSession().sendMessage(new TextMessage(json));
    }

}
