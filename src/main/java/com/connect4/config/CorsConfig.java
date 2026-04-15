package com.connect4.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CorsConfig — autorise React (port 3000) à appeler le backend Spring Boot (port 8080).
 *
 * Le @CrossOrigin sur chaque controller suffit pour le développement,
 * mais cette config globale est plus propre et évite de l'oublier
 * sur les futurs controllers (SaveController, etc.).
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("https://connect4-ninoncrst.onrender.com", "http://localhost:3000")
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false);
    }
}
