import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.igrowker.feature.parkify.websocket.YourWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = "http://localhost:5173";
        }
        registry.addHandler(new YourWebSocketHandler(), "/ws")
                .setAllowedOrigins(frontendUrl)
                .withSockJS(); // ⚠️ opcional, si querés soporte para clientes sin WebSocket puro
    }
}

