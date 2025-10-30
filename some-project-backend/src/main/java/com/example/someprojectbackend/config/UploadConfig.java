package com.example.someprojectbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Konfigurasjonsklasse for opplastingskatalogen.
 *
 * Denne klassen definerer en {@link Path}-bean som kan injiseres
 * andre steder i applikasjonen når man trenger tilgang til
 * rotmappen for filopplastinger.
 *
 * Katalogen spesifiseres i application.properties/yml med:
 *   app.upload.dir=./uploads
 */
@Configuration
public class UploadConfig {

    /**
     * Oppretter en {@link Path}-bean som peker på rotmappen for opplastinger.
     *
     * @param dir sti hentet fra konfigurasjonsegenskapen {@code app.upload.dir}.
     * @return normalisert absolutt sti til opplastingsmappen.
     */
    @Bean
    public Path uploadRoot(@Value("${app.upload.dir}") String dir) {
        return Paths.get(dir).toAbsolutePath().normalize();
    }
}
