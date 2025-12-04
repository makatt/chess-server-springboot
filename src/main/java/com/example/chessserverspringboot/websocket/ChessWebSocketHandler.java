package com.example.chessserverspringboot.websocket;

import com.example.chessserverspringboot.service.GameDatabaseService;
import com.example.chessserverspringboot.service.MatchmakerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChessWebSocketHandler extends TextWebSocketHandler {

    private final MatchmakerService matchmaker;
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private GameDatabaseService gameDB;

    public ChessWebSocketHandler(MatchmakerService matchmaker) {
        this.matchmaker = matchmaker;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
        System.out.println(" Игрок подключился: " + session.getId());
    }


    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        MessageModel msg = mapper.readValue(message.getPayload(), MessageModel.class);

        switch (msg.getType()) {

            // ==================================================
            //                      MOVE
            // ==================================================
            case "MOVE" -> {
                GameSession game = matchmaker.getGameByPlayer(msg.getSender());
                if (game != null) {

                    String[] move = msg.getContent().split("-");
                    String result = game.getState().makeMove(move[0], move[1]);

                    // FEN после хода
                    String fen = game.getState().toFEN();

                    // Сохранение хода
                    gameDB.saveMove(
                            game.getMatchId(),
                            Integer.parseInt(msg.getSender()),
                            0,                      // moveNumber можно позже добавить
                            msg.getContent(),
                            fen
                    );

                    if (result.equals("OK") || result.equals("CHECK")) {

                        if (game.getTimer() != null)
                            game.getTimer().switchTurn();

                        String newState = mapper.writeValueAsString(game.getState());

                        broadcastToGame(game, new MessageModel("UPDATE", "SERVER", newState));

                        if (result.equals("CHECK")) {
                            broadcastToGame(game,
                                    new MessageModel("CHECK", "SERVER", "Король под шахом!")
                            );
                        }

                    } else if (result.equals("CHECKMATE")) {

                        broadcastToGame(game,
                                new MessageModel("CHECKMATE", "SERVER", "Мат! Игра окончена.")
                        );

                        int winnerId = Integer.parseInt(msg.getSender());
                        String finalFen = game.getState().toFEN();

                        gameDB.finishMatch(game.getMatchId(), winnerId, finalFen, "checkmate");

                        if (game.getTimer() != null)
                            game.getTimer().stop();

                    } else {

                        session.sendMessage(new TextMessage(
                                mapper.writeValueAsString(new MessageModel(
                                        "ERROR",
                                        "SERVER",
                                        "Неверный ход или не ваш ход"
                                ))
                        ));
                    }
                }
            }

            // ==================================================
            //                  CREATE ROOM
            // ==================================================
            case "CREATE_ROOM" -> {
                String[] parts = msg.getContent().split("\\|");
                int minutes = Integer.parseInt(parts[0]);
                int increment = (parts.length > 1 ? Integer.parseInt(parts[1]) : 0);

                Player player = new Player(msg.getSender(), session);
                matchmaker.createRoom(player, minutes, increment);

                session.sendMessage(new TextMessage(
                        mapper.writeValueAsString(
                                new MessageModel("WAIT", "SERVER",
                                        "Комната создана (" + minutes + "|" + increment + "). Ожидание соперника...")
                        )
                ));
            }

            // ==================================================
            //                    JOIN ROOM
            // ==================================================
            case "JOIN_ROOM" -> {
                String[] parts = msg.getContent().split("\\|");
                int minutes = Integer.parseInt(parts[0]);
                int increment = (parts.length > 1 ? Integer.parseInt(parts[1]) : 0);

                Player player = new Player(msg.getSender(), session);
                GameSession game = matchmaker.joinRoom(player, minutes, increment);

                if (game == null) {

                    session.sendMessage(new TextMessage(
                            mapper.writeValueAsString(
                                    new MessageModel("WAIT", "SERVER",
                                            "Ожидание комнаты " + minutes + "|" + increment)
                            )
                    ));

                } else {

                    String startInfo = String.format(
                            "white=%s, black=%s, time=%d|%d",
                            game.getWhite().getName(),
                            game.getBlack().getName(),
                            minutes,
                            increment
                    );

                    broadcastToGame(game, new MessageModel("START", "SERVER", startInfo));
                }
            }

            // ==================================================
            //                  GET ROOMS
            // ==================================================
            case "ROOMS" -> {

                var rooms = matchmaker.getAvailableRooms();
                String json = mapper.writeValueAsString(rooms);

                session.sendMessage(new TextMessage(
                        mapper.writeValueAsString(
                                new MessageModel("ROOMS", "SERVER", json)
                        )
                ));
            }


            // ==================================================
            //                     DEFAULT
            // ==================================================
            default -> {
                session.sendMessage(new TextMessage(" Неизвестный тип сообщения"));
            }
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
