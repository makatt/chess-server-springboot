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

                    switch (result) {
                        case "OK" -> broadcastToGame(game, new MessageModel("UPDATE", "SERVER",
                                mapper.writeValueAsString(game.getState())));
                        case "CHECK" -> broadcastToGame(game, new MessageModel("CHECK", "SERVER", "Король под шахом!"));
                        case "CHECKMATE" -> broadcastToGame(game, new MessageModel("CHECKMATE", "SERVER", "Мат! Игра окончена."));
                        case "SELF_CHECK" -> session.sendMessage(new TextMessage(mapper.writeValueAsString(
                                new MessageModel("ERROR", "SERVER", "Нельзя оставить своего короля под шахом!"))));
                        default -> session.sendMessage(new TextMessage(mapper.writeValueAsString(
                                new MessageModel("ERROR", "SERVER", "Неверный ход"))));
                    }
                }
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
