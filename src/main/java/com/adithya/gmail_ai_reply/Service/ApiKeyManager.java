package com.adithya.gmail_ai_reply.Service;

import jakarta.annotation.PostConstruct;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ApiKeyManager {

    private final List<String> keys;
    private final AtomicInteger currentIndex = new AtomicInteger(0);

    public ApiKeyManager(
            @Value("${gemini.api.key1}") String key1,
            @Value("${gemini.api.key2}") String key2
            // add more keys as constructor arguments
    ) {
        this.keys = List.of(key1, key2); // expand as needed
    }

    public String getNextKey() {
        int index = currentIndex.getAndUpdate(i -> (i + 1) % keys.size());

        return keys.get(index);
    }

    // Rotate manually only when needed (e.g. quota exceeded)
    public void markKeyExhausted() {
        currentIndex.updateAndGet(i -> (i + 1) % keys.size());
    }

    public int totalKeys() {
        return keys.size();
    }
}