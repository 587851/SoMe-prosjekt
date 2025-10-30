package com.example.someprojectbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * Konfigurasjonsklasse for servering av statiske filer.
 *
 * Denne klassen gjør det mulig å eksponere opplastede filer
 * fra en lokal mappe slik at de kan nås via HTTP.
 *
 * For eksempel: en fil lagret i ./uploads/myfile.png
 * vil kunne nås på http://localhost:8080/files/myfile.png
 */
@Configuration
public class StaticFiles implements WebMvcConfigurer {

    /**
     * Opplastingskatalog for filer.
     * Verdi settes via application.properties eller application.yml:
     *   app.uploadDir=./uploads
     *
     * Hvis ikke konfigurert brukes "./uploads" som standard.
     */
    @Value("${app.uploadDir:./uploads}")
    private String uploadDir;

    /**
     * Registrerer en ressurs-handler som eksponerer filer
     * fra uploadDir slik at de kan nås via /files/** i API-et.
     *
     * @param reg ResourceHandlerRegistry brukt til å registrere handleren.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry reg) {
        var uri = Path.of(uploadDir).toAbsolutePath().toUri().toString();

        reg.addResourceHandler("/files/**")
                .addResourceLocations(uri)
                .setCachePeriod(3600);
    }
}
