package ru.kuzmin.gptbot.service;

import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import ru.kuzmin.gptbot.exceptions.KzmGptException;
import ru.kuzmin.gptbot.utils.GptApiToken;

/**
 * @author Kuzmin Artem
 * @since 10 май 2024 г.
 */
@Component
public class TokenStorage {

    private final LinkedBlockingQueue<GptApiToken> queue = new LinkedBlockingQueue<>();

    @PostConstruct
    private void init() {
        String[] tokens = System.getProperty("apiTokens").split(", ");
        for (String token : tokens) {
            if (!token.isEmpty()) {
                putIntQueue(new GptApiToken(token.trim()));
            }
        }
    }

    public String getToken() {
        if (queue.isEmpty()) {
            throw new KzmGptException("Token queue is empty!");
        }
        GptApiToken token = queue.poll();
        putIntQueue(token);
        return token.getValue();
    }

    private void putIntQueue(GptApiToken token) {
        try {
            queue.put(token);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
