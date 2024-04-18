package ru.kuzmin.gptbot.utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import ru.kuzmin.gptbot.Enum.Role;
import ru.kuzmin.gptbot.Enum.GPTModel;
import ru.kuzmin.gptbot.interaction.Message;

/**
 * @author Kuzmin Artem
 * @since 18 апр. 2024 г.
 */

public class ChatBotCache {

    public ChatBotCache(int maxContentLength, String prompt) {
        this.maxContentLength = maxContentLength;
        this.prompt = prompt;
    }

    private int maxContentLength;

    private String prompt;

    private final ConcurrentHashMap<String, ChatContext> chatContext = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, GPTModel> modelCache = new ConcurrentHashMap<>();

    public void initCache(String chatId) {
        modelCache.putIfAbsent(chatId, GPTModel.GPT_3_5);
        chatContext.putIfAbsent(chatId, new ChatContext(maxContentLength, prompt));
    }

    public List<Message> getChatContext(String chatId) {
        return chatContext.get(chatId).getMessages();
    }

    public void addMessageToContext(String chatId, Role role, String text) {
        chatContext.get(chatId).addMessageToContext(role, text);
    }

    public void flushContext(String chatId) {
        chatContext.get(chatId).flushContext();
    }

    public GPTModel getCurrentModel(String chatId) {
        return modelCache.get(chatId);
    }

    public void switchToModel(String chatId, GPTModel gptModel) {
        modelCache.put(chatId, gptModel);
    }

    private void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
}
