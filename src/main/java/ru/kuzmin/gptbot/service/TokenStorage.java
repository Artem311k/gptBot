package ru.kuzmin.gptbot.service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
    private final Lock lock = new ReentrantLock();

    @PostConstruct
    private void init() {
        String[] tokens = System.getProperty("apiTokens").split(",");
        for (String token : tokens) {
            if (!token.isEmpty()) {
                putInQueue(new GptApiToken(token.trim()));
            }
        }
    }

    public String getToken() {
        lock.lock();
        try {
            if (queue.isEmpty()) {
                throw new KzmGptException("Token queue is empty!");
            }
            GptApiToken token = queue.poll();
            putInQueue(token);
            return token.getValue();
        } finally {
            lock.unlock();
        }
    }

    private void putInQueue(GptApiToken token) {
        try {
            queue.put(token);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}
