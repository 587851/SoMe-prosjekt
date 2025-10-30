package com.example.someprojectbackend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Konfigurasjonsklasse for CORS (Cross-Origin Resource Sharing).
 *
 * Denne klassen gjør det mulig for frontend-applikasjoner
 * (f.eks. React som kjører på localhost:3000) å kommunisere
 * med backend-API-et uten å bli blokkert av nettleserens
 * same-origin policy.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Konfigurerer CORS-regler for hele applikasjonen.
     *
     * @param registry CorsRegistry som brukes til å registrere tillatte mappings.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                        "http://localhost:3000",
                        "http://127.0.0.1:3000"
                )
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
