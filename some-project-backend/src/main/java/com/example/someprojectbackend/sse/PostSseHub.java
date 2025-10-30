package com.example.someprojectbackend.sse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.UUID;

/**
 * Hub for Server-Sent Events (SSE) relatert til innlegg.
 *
 * Håndterer:
 *  - Abonnement på SSE-strømmer fra klienter
 *  - Broadcasting av hendelser (nye poster, sletting, generiske events)
 *
 * Bruker {@link SseEmitter} for å holde åpne HTTP-tilkoblinger mot klienter.
 */
@Component
public class PostSseHub {

    /** Alle aktive klientforbindelser, key = lokal ID for emitter. */
    private final Map<Long, SseEmitter> emitters = new ConcurrentHashMap<>();

    /** Teller for å gi hver emitter en unik ID. */
    private final AtomicLong ids = new AtomicLong();

    /**
     * Abonnerer en ny klient på SSE-strømmen.
     *
     * - Oppretter en ny {@link SseEmitter} uten timeout (0L).
     * - Registrerer callbacks for completion, timeout og error.
     * - Sender et "hello"-event umiddelbart for å åpne strømmen.
     *
     * @return en åpen {@link SseEmitter} som kan returneres til klienten i en Controller
     */
    public SseEmitter subscribe() {
        SseEmitter emitter = new SseEmitter(0L);
        long id = ids.incrementAndGet();
        emitters.put(id, emitter);

        emitter.onCompletion(() -> emitters.remove(id));
        emitter.onTimeout(() -> emitters.remove(id));
        emitter.onError(e -> emitters.remove(id));

        try {
            emitter.send(SseEmitter.event()
                    .name("hello")
                    .data("ok " + Instant.now().toString()));
        } catch (IOException ignored) {
            emitters.remove(id);
            try { emitter.complete(); } catch (Exception ex) { /* ignorér */ }
        }
        return emitter;
    }

    /**
     * Sender et generisk event til alle aktive klienter.
     * Best-effort: fjerner emitters som feiler uten å kaste til MVC-laget.
     *
     * @param eventName navn på SSE-event (brukes på klient)
     * @param payload data som sendes (serialiseres som JSON)
     */
    public void broadcast(String eventName, Object payload) {
        for (var entry : emitters.entrySet()) {
            Long id = entry.getKey();
            SseEmitter emitter = entry.getValue();
            try {
                emitter.send(
                        SseEmitter.event()
                                .name(eventName)
                                .data(payload, MediaType.APPLICATION_JSON)
                );
            } catch (IllegalStateException | IOException ex) {
                try { emitter.complete(); } catch (Exception ignore) {}
                emitters.remove(id);
            } catch (Exception ex) {
                try { emitter.completeWithError(ex); } catch (Exception ignore) {}
                emitters.remove(id);
            }
        }
    }

    /**
     * Sender et "post"-event til alle klienter.
     *
     * @param postDto DTO for nytt innlegg
     */
    public void broadcastPost(Object postDto) {
        broadcast("post", postDto);
    }

    /**
     * Sender et "postDeleted"-event til alle klienter.
     *
     * @param postId ID til slettet innlegg
     */
    public void broadcastPostDeleted(UUID postId) {
        broadcast("postDeleted", postId);
    }
}
