
package com.example.chessserverspringboot.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import com.example.chessserverspringboot.websocket.ChessWebSocketHandler;

@Configuration
@EnableWebSocket
public class ChessWebSocketConfig implements WebSocketConfigurer {

    private final ChessWebSocketHandler chessHandler;

    public ChessWebSocketConfig(ChessWebSocketHandler chessHandler) {
        this.chessHandler = chessHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chessHandler, "/chess")
                .setAllowedOrigins("*"); // Для Android-клиента
    }
}
