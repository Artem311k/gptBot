package ru.kuzmin.gptbot.bot;

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
public class KuzminChatGptBot extends AbstractKzmGptBot {

    private final GptChatProcessor processor;

    public KuzminChatGptBot(
            GptChatProcessor processor,
            @Value("${app.default.prompt}") String prompt,
            @Value("${app.max.context.length}") Integer maxContentLength,
            @Value("${app.temperature}") Double temperature) {
        super(
                System.getProperty("botToken"),
                prompt,
                maxContentLength,
                temperature,
                System.getProperty("botPassword", ""),
                new ChatBotCache(maxContentLength, prompt),
                new ChatMessageSender()
        );
        this.processor = processor;
    }

    @Override
    public void onUpdateReceived(Update update) {
        processor.process(this, update);
    }

    @Override
    public String getBotUsername() {
        return "kuzmin_chat_bot";
    }
}
