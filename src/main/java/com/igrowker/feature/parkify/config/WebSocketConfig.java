import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import com.igrowker.feature.parkify.websocket.YourWebSocketHandler;

@Configuration
@EnableWebSocket
@Component
public class WebSocketConfig implements WebSocketConfigurer {

    private final YourWebSocketHandler yourWebSocketHandler;

    public WebSocketConfig(YourWebSocketHandler yourWebSocketHandler) {
        this.yourWebSocketHandler = yourWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        String frontendUrl = System.getenv("FRONTEND_URL");
        if (frontendUrl == null || frontendUrl.isEmpty()) {
            frontendUrl = "http://localhost:5173";
        }
        registry.addHandler(yourWebSocketHandler, "/ws")
                //.setAllowedOrigins(frontendUrl);
                .setAllowedOrigins("*");
    }
}

