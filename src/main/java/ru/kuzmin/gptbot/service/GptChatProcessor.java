package ru.kuzmin.gptbot.service;

import java.util.List;
import java.util.concurrent.*;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.kuzmin.gptbot.enums.Role;
import ru.kuzmin.gptbot.enums.GPTModel;
import ru.kuzmin.gptbot.bot.KzmGptBot;
import ru.kuzmin.gptbot.interaction.Message;
import ru.kuzmin.gptbot.utils.ChatBotCache;
import ru.kuzmin.gptbot.utils.ChatMessageSender;

import static ru.kuzmin.gptbot.enums.Role.ASSISTANT;
import static ru.kuzmin.gptbot.enums.Role.USER;
import static ru.kuzmin.gptbot.enums.GPTModel.GPT_3_5;
import static ru.kuzmin.gptbot.enums.GPTModel.GPT_4_TURBO;
import static ru.kuzmin.gptbot.utils.Commands.*;

/**
 * @author Kuzmin Artem
 * @since 18 апр. 2024 г.
 */

@Slf4j
@Service
@RequiredArgsConstructor
@Scope("prototype")
public class GptChatProcessor {

    private final GptClient gptClient;
    private final ChatMessageSender messageSender;
    private final ConcurrentHashMap<String, ChatBotCache> cache = new ConcurrentHashMap<>();
    private final ExecutorService typer = Executors.newCachedThreadPool();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void process(KzmGptBot bot, Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String userId = update.getMessage().getFrom().getId().toString();
        String text = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        initCache(bot, chatId);

        if (!checkIfUserEnabled(bot, userId)) {
            register(bot, userId, chatId, text);
            return;
        }

        switch (text) {
        case START -> handleStart(bot, chatId);
        case GPT_3_5_MSG -> switchToModel(bot, chatId, GPT_3_5);
        case GPT_4_TURBO_MSG -> switchToModel(bot, chatId, GPT_4_TURBO);
        case FLUSH -> handleFlush(bot, chatId);
        case HELP -> sendHelpMessage(bot, chatId);
        default -> processMessage(bot, chatId, text);
        }
    }

    private void processMessage(KzmGptBot bot, String chatId, String text) {

        Future<?> typingFuture = pretendTyping(bot, chatId);

        addMessageToContext(bot, chatId, USER, text);

        Future<String> responseFuture = executor.submit(() ->
                gptClient.getResponse(currentContext(bot, chatId), currentModel(bot, chatId), bot.getTemperature(), bot.getApiToken()));

        try {
            String response = responseFuture.get(1, TimeUnit.MINUTES);
            sendChatResponseToUser(bot, chatId, response);
            addMessageToContext(bot, chatId, ASSISTANT, response);
        } catch (ExecutionException e) {
            log.error("Error occurred while getting a response.", e.getCause());
            sendErrorMessage(bot, chatId, e.getCause().getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("The operation was interrupted.", e);
        } catch (TimeoutException e) {
            log.error("The operation timed out.", e);
            sendErrorMessage(bot, chatId, "The operation timed out.");
            responseFuture.cancel(true);
        } finally {
            typingFuture.cancel(true);
            switchToDefaultModel(bot, chatId);
        }
    }

    private Future<?> pretendTyping(TelegramLongPollingBot bot, String chatId) {
        return typer.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                messageSender.sendTypingAction(bot, chatId);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void register(KzmGptBot bot, String userId, String chatId, String password) {
        if (!password.equals(bot.getPassword())) {
            sendRegisterMessage(bot, chatId);
            return;
        }
        getBotCache(bot).enableUser(userId);
        messageSender.sendMessage(bot, chatId, "Теперь можно пользоваться ботом!");
    }

    private boolean checkIfUserEnabled(TelegramLongPollingBot bot, String userId) {
        return getBotCache(bot).isUserEnabled(userId);
    }

    private void sendChatResponseToUser(TelegramLongPollingBot bot, String chatId, String responses) {
        messageSender.sendMessage(bot, chatId, responses);
    }

    private void handleFlush(KzmGptBot bot, String chatId) {
        getBotCache(bot).flushContext(chatId);
        messageSender.sendMessage(bot, chatId, buildFlushContextMessage());
    }

    private void switchToModel(KzmGptBot bot, String chatId, GPTModel model) {
        messageSender.sendMessage(bot, chatId, "Установлена модель " + model.getValue());
        getBotCache(bot).switchToModel(chatId, model);
    }

    private void switchToDefaultModel(KzmGptBot bot, String chatId) {
        if (!currentModel(bot, chatId).equals(GPT_3_5)) {
            switchToModel(bot, chatId, GPT_3_5);
        }
    }

    private void initCache(KzmGptBot bot, String chatId) {
        cache.putIfAbsent(bot.getBotUsername(), new ChatBotCache(bot.getMaxContentLength(), bot.getDefaultPrompt()));
        cache.get(bot.getBotUsername()).initCache(chatId);
    }

    private void addMessageToContext(TelegramLongPollingBot bot, String chatId, Role role, String message) {
        getBotCache(bot).addMessageToContext(chatId, role, message);
    }

    private void handleStart(TelegramLongPollingBot bot, String chatId) {
        messageSender.sendMessage(bot, chatId, buildStartMessage());
        sendHelpMessage(bot, chatId);
    }

    private void sendHelpMessage(TelegramLongPollingBot bot, String chatId) {
        messageSender.sendMessage(bot, chatId, String.format(getHelpMessage(), currentModel(bot, chatId).getValue()));
    }

    private ChatBotCache getBotCache(TelegramLongPollingBot bot) {
        return cache.get(bot.getBotUsername());
    }

    private GPTModel currentModel(TelegramLongPollingBot bot, String chatId) {
        return getBotCache(bot).getCurrentModel(chatId);
    }

    private List<Message> currentContext(TelegramLongPollingBot bot, String chatId) {
        return getBotCache(bot).getChatContext(chatId);
    }

    private void sendErrorMessage(TelegramLongPollingBot bot, String chatId, String message) {
        messageSender.sendMessage(bot, chatId, "Exception while trying to get response : " + message);
    }

    private void sendRegisterMessage(TelegramLongPollingBot bot, String chatId) {
        messageSender.sendMessage(bot, chatId, buildRegisterMessage());
    }

    private String buildRegisterMessage() {
        return "Для ипользования бота необходимо ввести пароль";
    }

    private String getHelpMessage() {
        return """
                Текущая модель %s.
                Выбор модели:
                /gpt3 - Для активации GPT 3.5 turbo
                /gpt4 - Для активации GPT 4 turbo
                /flush - сборс контекста
                /help - текущая информация
                По умолчанию активировна gpt 3.5, модель сбрасывается до 3.5 после каждого новго запроса к gpt4.
                """;
    }

    private String buildStartMessage() {
        return "Спросите что-нибудь у бота.";
    }

    private String buildFlushContextMessage() {
        return "Контекст сброшен";
    }

}
