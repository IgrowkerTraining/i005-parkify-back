package com.igrowker.feature.parkify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer{

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Usar la variable de entorno para obtener el frontend URL
                String frontendUrl = System.getenv("FRONTEND_URL");
                System.out.println("FRONTEND_URL: " + frontendUrl);
                registry.addMapping("/**")
                        .allowedOrigins("http://34.107.135.109", "http://localhost")
                        //.allowedOrigins("*")
                        //.allowedOrigins(frontendUrl)  // Establecer el frontend URL dinámicamente
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .allowedHeaders("*");
            }
        };
    }
}