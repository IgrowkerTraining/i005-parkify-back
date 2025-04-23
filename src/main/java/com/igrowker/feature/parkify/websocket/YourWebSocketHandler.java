package com.igrowker.feature.parkify.websocket;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class YourWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("✅ Cliente conectado: " + session.getId());
        session.sendMessage(new TextMessage("Conexión WebSocket establecida con el backend 🚀"));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        System.out.println("📩 Mensaje recibido del cliente: " + message.getPayload());
        // Opcional: responder al cliente
        session.sendMessage(new TextMessage("Echo: " + message.getPayload()));
    }
}
