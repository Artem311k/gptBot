package ru.kuzmin.gptbot.bot;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.extern.slf4j.Slf4j;
import ru.kuzmin.gptbot.Enum.GPTModel;
import ru.kuzmin.gptbot.Enum.ChatRole;
import ru.kuzmin.gptbot.interaction.Message;
import ru.kuzmin.gptbot.service.GptService;

import static ru.kuzmin.gptbot.Enum.GPTModel.*;
import static ru.kuzmin.gptbot.Enum.ChatRole.*;
import static ru.kuzmin.gptbot.utils.Command.*;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */

@Component
@Slf4j
public class KuzminChatGptBot extends TelegramLongPollingBot {

    public KuzminChatGptBot(GptService service) {
        super(System.getProperty("botToken"));
        this.gptService = service;
    }
    private final GptService gptService;

    @Value("${app.default.prompt}")
    private String defaultPrompt;

    @Value("${app.max.context.length}")
    private Integer MAX_CONTEXT_LENGTH;

    @Value("${app.temperature}")
    private Double TEMPERATURE;

    private final ConcurrentHashMap<String, GPTModel> modelCash = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, List<Message>> chatContextCash = new ConcurrentHashMap<>();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void onUpdateReceived(Update update) {
        process(update);
    }

    private void process(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String msg = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        switch (msg) {
        case START -> handleStart(chatId);
        case GPT_3_5_MSG -> switchToModel(chatId, GPT_3_5);
        case GPT_4_TURBO_MSG -> switchToModel(chatId, GPT_4_TURBO);
        case FLUSH -> handleFlush(chatId);
        case HELP -> sendHelpMessage(chatId);
        default -> processMessage(chatId, msg, modelCash.get(chatId));
        }
    }

    private void processMessage(String chatId, String text, GPTModel model) {
        if (model == null) {
            model = switchModelToDefault(chatId);
        }

        Future<?> typingFuture = pretendTyping(chatId);

        addMessageToContext(chatId, USER, text);

        String response;
        try {
            response = gptService.getResponse(getChatContext(chatId), model, TEMPERATURE);
            sendChatResponseToUser(chatId, response);
        } catch (Exception e) {
            sendMessage(chatId, "Exception while trying to get response : " + e.getMessage());
        } finally {
            typingFuture.cancel(true);
        }
        switchModelToDefault(chatId);
    }

    private void sendChatResponseToUser(String chatId, String response) {
        sendMessage(chatId, response);
        addMessageToContext(chatId, ASSISTANT, response);
    }

    private GPTModel switchModelToDefault(String chatId) {
        if (modelCash.get(chatId) == null || !modelCash.get(chatId).equals(GPT_3_5)) {
            return switchToModel(chatId, GPT_3_5);
        }
        return GPT_3_5;
    }

    private GPTModel switchToModel(String chatId, GPTModel model) {
        modelCash.put(chatId, model);
        sendMessage(chatId, "Установлена модель " + model.getValue());
        return model;
    }

    private GPTModel getCurrentModel(String chatId) {
        return modelCash.computeIfAbsent(chatId, this::switchModelToDefault);
    }

    private void addMessageToContext(String chatId, ChatRole chatRole, String text) {
        List<Message> chatContext = getChatContext(chatId);
        if (chatContext.size() >= MAX_CONTEXT_LENGTH) {
            chatContext.remove(1);
        }
        chatContext.add(new Message(chatRole, text));
    }

    private List<Message> getChatContext(String chatId) {
        return chatContextCash.computeIfAbsent(chatId, this::flushChatContext);
    }

    private List<Message> flushChatContext(String chatId) {
        List<Message> chatContext = new ArrayList<>();
        chatContext.add(new Message(SYSTEM, defaultPrompt));
        return chatContextCash.put(chatId, chatContext);

    }

    private void pretendTypingAction(String chatId) {
        SendChatAction sendChatAction = new SendChatAction();
        sendChatAction.setChatId(chatId);
        sendChatAction.setAction(ActionType.TYPING);
        try {
            execute(sendChatAction);
        } catch (TelegramApiException ex) {
            log.error("Exception while sending message", ex);

        }
    }

    private Future<?> pretendTyping(String chatId) {
        return executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                pretendTypingAction(chatId);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void handleStart(String chatId) {
        sendHelloMessage(chatId);
        switchModelToDefault(chatId);
        sendHelpMessage(chatId);
    }

    private void handleFlush(String chatId) {
        flushChatContext(chatId);
        sendMessage(chatId, "Контекст сброшен");
    }

    private void sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage(chatId, message);
        sendMessage.enableMarkdown(true);
        sendMessage(sendMessage);
    }


    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException ex) {
            log.error("Exception while sending message", ex);
        }
    }

    private void sendHelloMessage(String chatId) {
        String text = """
                Спросите что-нибудь у бота.
                """;
        sendMessage(chatId, text);
    }

    private void sendHelpMessage(String chatId) {
        String text = """
                Текущая модель %s.
                Выбор модели:
                /gpt3 - Для активации GPT 3.5 turbo
                /gpt4 - Для активации GPT 4 turbo
                /flush - сборс контекста
                /help - текущая информация
                По умолчанию активировна gpt 3.5, модель сбрасывается до 3.5 после каждого новго запроса к gpt4.
                """;
        sendMessage(chatId, String.format(text, getCurrentModel(chatId).getValue()));

    }

    @Override
    public String getBotUsername() {
        return "kuzmin_chat_bot";
    }
}
