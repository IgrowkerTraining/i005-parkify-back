package com.igrowker.feature.parkify.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Aquí se pasa la dirección de tu frontend como permitido para el CORS
        registry.addHandler(new SimpleWebSocketHandler(), "/ws")
                .setAllowedOrigins("*");  // Cambia la URL por la de tu frontend
    }
}