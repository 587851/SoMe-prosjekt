package com.example.someprojectbackend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Konfigurasjonsklasse for tidsrelaterte beans.
 * <p>
 * Denne klassen eksponerer en {@link Clock}-bean som kan brukes
 * i andre deler av applikasjonen for å håndtere tid. Ved å bruke en
 * {@code Clock}-bean i stedet for å kalle {@code Instant.now()} eller
 * {@code LocalDateTime.now()} direkte, blir koden mer testbar,
 * siden klokken kan "mockes" i tester.
 */
@Configuration
public class ClockConfig {

    /**
     * Oppretter en systemklokke som alltid bruker UTC.
     *
     * @return en {@link Clock} instans basert på systemets UTC-tid.
     */
    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
