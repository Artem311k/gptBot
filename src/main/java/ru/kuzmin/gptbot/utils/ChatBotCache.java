package ru.kuzmin.gptbot.utils;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import ru.kuzmin.gptbot.enums.Role;
import ru.kuzmin.gptbot.enums.GPTModel;
import ru.kuzmin.gptbot.enums.UserStatus;
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

    private final ConcurrentHashMap<String, ChatContext> chatContextCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, GPTModel> modelCache = new ConcurrentHashMap<>();

    private final ConcurrentHashMap<String, UserStatus> usersCache = new ConcurrentHashMap<>();

    public void initCache(String chatId) {
        modelCache.putIfAbsent(chatId, GPTModel.GPT_3_5);
        chatContextCache.putIfAbsent(chatId, new ChatContext(maxContentLength, prompt));
    }

    public boolean isUserEnabled(String userId) {
        if (usersCache.get(userId) == null) {
            usersCache.put(userId, UserStatus.UNKNOWN);
            return false;
        }
        return usersCache.get(userId).equals(UserStatus.REGISTERED);
    }

    public void enableUser(String userId) {
        usersCache.put(userId, UserStatus.REGISTERED);
    }

    public void disableUser(String userId) {
        usersCache.put(userId, UserStatus.UNKNOWN);
    }

    public List<Message> getChatContext(String chatId) {
        return chatContextCache.get(chatId).getMessages();
    }

    public void addMessageToContext(String chatId, Role role, String text) {
        chatContextCache.get(chatId).addMessageToContext(role, text);
    }

    public void flushContext(String chatId) {
        chatContextCache.get(chatId).flushContext();
    }

    public GPTModel getCurrentModel(String chatId) {
        return modelCache.get(chatId);
    }

    public void switchToModel(String chatId, GPTModel gptModel) {
        modelCache.put(chatId, gptModel);
    }

    public void setMaxContentLength(int maxContentLength) {
        this.maxContentLength = maxContentLength;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getPrompt() {
        return prompt;
    }
}
