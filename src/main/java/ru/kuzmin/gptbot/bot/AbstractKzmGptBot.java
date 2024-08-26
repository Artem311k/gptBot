package ru.kuzmin.gptbot.bot;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import lombok.Getter;
import ru.kuzmin.gptbot.enums.GPTModelName;
import ru.kuzmin.gptbot.enums.Role;
import ru.kuzmin.gptbot.interaction.Message;

/**
 * @author Kuzmin Artem
 * @since 18 апр. 2024 г.
 */
@Getter
public abstract class AbstractKzmGptBot extends TelegramLongPollingBot {

    private final String defaultPrompt;
    private final Integer maxContentLength;
    private final Double temperature;
    private final String password;
    private final ChatBotCache cache;
    private final ChatMessageSender sender;
    private boolean useDefaultPrompt;

    public AbstractKzmGptBot(String botToken,
            String defaultPrompt,
            Integer maxContentLength,
            Double temperature,
            String password,
            ChatBotCache cache,
            ChatMessageSender sender) {
        super(botToken);
        this.useDefaultPrompt = StringUtils.isNotBlank(defaultPrompt);
        this.defaultPrompt = defaultPrompt;
        this.maxContentLength = maxContentLength;
        this.temperature = temperature;
        this.password = password;
        this.cache = cache;
        this.sender = sender;
    }

    public void sendMessage(String chatId, String message) {
        sender.sendMessage(this, chatId, message);
    }

    public void pretendTyping(String chatId) {
        sender.sendTypingAction(this, chatId);
    }

    public void addMessageToContext(String chatId, Role role, String text) {
        cache.addMessageToContext(chatId, role, text);
    }

    public boolean checkIfUserEnabled(String userId) {
        return cache.isUserEnabled(userId);
    }

    public void enableUser(String userId) {
        cache.enableUser(userId);
    }

    public void flushContext(String chatId) {
        cache.flushContext(chatId);
    }

    public void switchModel(String chatId, GPTModelName gptModelName) {
        cache.switchToModel(chatId, gptModelName);
    }

    public List<Message> getCurrentContext(String chatId) {
        return cache.getChatContext(chatId);
    }

    public GPTModelName getCurrentModel(String chatId) {
        return cache.getCurrentModel(chatId);
    }

    public void initCache(String chatId) {
        cache.initCache(chatId);
    }

    public boolean switchDefaultPrompt(String chatId) {
        return useDefaultPrompt ? removeDefaultPrompt(chatId) : addDefaultPrompt(chatId);
    }

    private boolean removeDefaultPrompt(String chatId) {
        cache.getChatContext(chatId)
                .removeIf(message -> message.getRole().equals(Role.SYSTEM));
        this.useDefaultPrompt = false;
        return false;
    }

    public boolean addDefaultPrompt(String chatId) {
        if (StringUtils.isNotBlank(defaultPrompt)) {
            cache.getChatContext(chatId)
                    .add(0, new Message(Role.SYSTEM, defaultPrompt));
            this.useDefaultPrompt = true;
            return true;
        }
        return false;
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

    public String getPassword() {
        return password;
    }
}
