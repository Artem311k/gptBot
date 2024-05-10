package ru.kuzmin.gptbot.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import ru.kuzmin.gptbot.bot.AbstractKzmGptBot;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */
@Configuration
public class BotConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(List<AbstractKzmGptBot> bots) throws TelegramApiException {
        var api = new TelegramBotsApi(DefaultBotSession.class);
        for (AbstractKzmGptBot bot : bots) {
            api.registerBot(bot);
        }
        return api;
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

}
