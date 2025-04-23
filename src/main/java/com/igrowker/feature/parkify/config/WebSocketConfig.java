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
        registry.addHandler(new YourWebSocketHandler(), "/socket-io")
                .setAllowedOrigins(frontendUrl)
                .withSockJS(); // ⚠️ opcional, si querés soporte para clientes sin WebSocket puro
    }
}

