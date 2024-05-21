package ru.kuzmin.gptbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import ru.kuzmin.gptbot.interaction.ChatResponse;

/**
 * @author Kuzmin Artem
 * @since 10 май 2024 г.
 */

@Component
@Slf4j
public class ResponseParser {
    
    @Value("${app.gpt35ComplPrice}")
    private Double GPT_3_5_TURBO_COMPL_PRICE;
    
    @Value("${app.gpt35PromptPrice}")
    private Double GPT_3_5_TURBO_PROMPT_PRICE;

    @Value("${app.gpt4oPromptPrice}")
    private Double GPT_4_O_PROMPT_PRICE;

    @Value("${app.gpt4oComplPrice}")
    private Double GPT_4_O_COMPL_PRICE;
    public String getMessage(ChatResponse response) {
        return response.getChoices().get(0).getMessage().getContent();
    }
    
    public Double getPrice(ChatResponse response) {
        Integer completionTokens = response.getUsage().getCompletionTokens();
        Integer promptTokens = response.getUsage().getPromptTokens();
        if (response.getModel().contains("gpt-3.5")) {
            return completionTokens * GPT_3_5_TURBO_COMPL_PRICE / 1000 + promptTokens * GPT_3_5_TURBO_PROMPT_PRICE / 1000;
        } else if (response.getModel().contains("gpt-4o")) {
            return completionTokens * GPT_4_O_COMPL_PRICE / 1000 + promptTokens * GPT_4_O_PROMPT_PRICE / 1000;
        }
        return null;
    }

}
