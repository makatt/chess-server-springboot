package com.example.chessserverspringboot.websocket;

import com.example.chessserverspringboot.service.MatchmakerService;
import com.example.chessserverspringboot.service.GameDatabaseService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChessWebSocketHandler extends TextWebSocketHandler {

    private final MatchmakerService matchmaker;
    private final GameDatabaseService gameDB;
    private final ObjectMapper mapper = new ObjectMapper();

    public ChessWebSocketHandler(MatchmakerService matchmaker, GameDatabaseService gameDB) {
        this.matchmaker = matchmaker;
        this.gameDB = gameDB;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        System.out.println("⚡ Игрок подключился: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        MessageModel msg;
        try {
            msg = mapper.readValue(message.getPayload(), MessageModel.class);
        } catch (Exception e) {
            session.sendMessage(new TextMessage("{\"type\":\"ERROR\",\"content\":\"BAD_JSON\"}"));
            return;
        }

        String type = msg.getType();
        String senderRaw = msg.getSender();
        String content = msg.getContent();

        System.out.println("📩 WS MESSAGE: " + type + " — " + content);

        /* ====================================================
                        ВАЛИДАЦИЯ SENDER
           ==================================================== */
        if (senderRaw == null || !senderRaw.matches("\\d+")) {
            session.sendMessage(new TextMessage(
                    "{\"type\":\"ERROR\",\"content\":\"INVALID_SENDER\"}"
            ));
            System.out.println("❌ INVALID SENDER: " + senderRaw);
            return;
        }

        // Преобразуем sender → число
        int senderId = Integer.parseInt(senderRaw);

        // Получаем или создаём игрока
        Player player = matchmaker.getOrCreatePlayer(senderRaw, session);
        player.setSession(session);

        switch (type) {

            /* ====================================================
                               СОЗДАНИЕ КОМНАТЫ
               ==================================================== */
            case "CREATE_ROOM" -> {
                try {
                    String[] parts = content.split("\\|");
                    int minutes = Integer.parseInt(parts[0]);
                    int increment = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

                    var room = matchmaker.createRoom(player, minutes, increment);

                    session.sendMessage(new TextMessage(
                            mapper.writeValueAsString(
                                    new MessageModel("ROOM_CREATED", "SERVER", room.getRoomId())
                            )
                    ));

                    System.out.println("🟩 Комната создана: " + room.getRoomId());

                } catch (Exception e) {
                    e.printStackTrace();
                    session.sendMessage(new TextMessage(
                            "{\"type\":\"ERROR\",\"content\":\"CREATE_ROOM_FAILED\"}"
                    ));
                }
            }

            /* ====================================================
                               ПОДКЛЮЧЕНИЕ К КОМНАТЕ
               ==================================================== */
            case "JOIN_ROOM_ID" -> {

                GameSession game = matchmaker.joinRoomById(content, player);

                if (game == null) {
                    session.sendMessage(new TextMessage(
                            "{\"type\":\"ERROR\",\"content\":\"ROOM_JOIN_FAILED\"}"
                    ));
                    return;
                }

                System.out.println("🟦 Игра началась в комнате: " + content);
            }

            /* ====================================================
                               СПИСОК КОМНАТ
               ==================================================== */
            case "GET_ROOMS" -> {

                try {
                    var rooms = matchmaker.getRooms();
                    String roomsJson = mapper.writeValueAsString(rooms);

                    session.sendMessage(new TextMessage(
                            mapper.writeValueAsString(
                                    new MessageModel("ROOMS_LIST", "SERVER", roomsJson)
                            )
                    ));
                } catch (Exception e) {
                    e.printStackTrace();
                    session.sendMessage(new TextMessage(
                            "{\"type\":\"ERROR\",\"content\":\"ROOMS_LIST_FAILED\"}"
                    ));
                }
            }

            /* ====================================================
                                 ХОД
               ==================================================== */
            case "MOVE" -> {

                String roomId = msg.getRoomId();
                String move = msg.getContent();

                System.out.println("♟ MOVE RECEIVED room=" + roomId + " move=" + move);

                GameSession game = matchmaker.getGameByRoomId(roomId);

                if (game == null) {
                    session.sendMessage(new TextMessage(
                            "{\"type\":\"ERROR\",\"content\":\"NO_ACTIVE_GAME\"}"
                    ));
                    return;
                }

                Player senderPlayer =
                        msg.getSender().equals(game.getWhite().getName())
                                ? game.getWhite()
                                : game.getBlack();

                game.handleMove(senderPlayer, move);
            }




            /* ====================================================
                               НЕИЗВЕСТНЫЙ ТИП
               ==================================================== */
            default -> {
                session.sendMessage(new TextMessage(
                        "{\"type\":\"ERROR\",\"content\":\"UNKNOWN_TYPE\"}"
                ));
            }
        }
    }
}
