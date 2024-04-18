package ru.kuzmin.gptbot.bot;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import lombok.Getter;

/**
 * @author Kuzmin Artem
 * @since 18 апр. 2024 г.
 */
@Getter
public abstract class KzmGptBot extends TelegramLongPollingBot {

    private final String defaultPrompt;
    private final Integer maxContentLength;
    private final Double temperature;

    private final String apiToken;

    public KzmGptBot(String botToken, String defaultPrompt, Integer maxContentLength, Double temperature, String apiToken) {
        super(botToken);
        this.defaultPrompt = defaultPrompt;
        this.maxContentLength = maxContentLength;
        this.temperature = temperature;
        this.apiToken = apiToken;
    }

    public String getApiToken() {
        return apiToken;
    }

    public String getDefaultPrompt() {
        return defaultPrompt;
    }

    public Integer getMaxContentLength() {
        return maxContentLength;
    }

    public Double getTemperature() {
        return temperature;
    }
}
