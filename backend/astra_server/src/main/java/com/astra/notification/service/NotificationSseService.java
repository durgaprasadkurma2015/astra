package com.astra.notification.service;

import com.astra.notification.dto.NotificationResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class NotificationSseService {
    private final ConcurrentHashMap<String, Set<SseEmitter>> emitters = new ConcurrentHashMap<>();

    public SseEmitter subscribe(String username, long timeoutMs) {
        SseEmitter emitter = new SseEmitter(timeoutMs);
        emitters.computeIfAbsent(username, ignored -> ConcurrentHashMap.newKeySet()).add(emitter);
        Runnable remove = () -> remove(username, emitter);
        emitter.onCompletion(remove);
        emitter.onTimeout(remove);
        emitter.onError(error -> remove.run());
        try {
            emitter.send(SseEmitter.event().name("connected").data("ASTRA SSE connected"));
        } catch (IOException ex) {
            remove(username, emitter);
        }
        return emitter;
    }

    public void publish(String username, NotificationResponse notification) {
        Set<SseEmitter> userEmitters = emitters.get(username);
        if (userEmitters == null) return;
        for (SseEmitter emitter : userEmitters) {
            try {
                emitter.send(SseEmitter.event().name("notification").data(notification));
            } catch (IOException ex) {
                remove(username, emitter);
            }
        }
    }

    private void remove(String username, SseEmitter emitter) {
        Set<SseEmitter> userEmitters = emitters.get(username);
        if (userEmitters != null) {
            userEmitters.remove(emitter);
            if (userEmitters.isEmpty()) emitters.remove(username);
        }
    }
}
