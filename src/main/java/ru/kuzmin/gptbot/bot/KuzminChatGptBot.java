package ru.kuzmin.gptbot.bot;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.extern.slf4j.Slf4j;
import ru.kuzmin.gptbot.service.GptChatProcessor;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */

@Component
@Slf4j
public class KuzminChatGptBot extends KzmGptBot {

    public KuzminChatGptBot(GptChatProcessor processor, @Value("${app.default.prompt}") String prompt,
            @Value("${app.max.context.length}") Integer maxContentLength, @Value("${app.temperature}") Double temperature) {
        super(System.getProperty("botToken"), prompt, maxContentLength, temperature, System.getProperty("apiToken"));
        this.processor = processor;
    }

    private final GptChatProcessor processor;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void onUpdateReceived(Update update) {
        executor.submit(() -> delegate(this, update));
    }

    @Override
    public String getBotUsername() {
        return "kuzmin_chat_bot";
    }

    private void delegate(KzmGptBot bot, Update update) {
        processor.process(bot, update);
    }

}
