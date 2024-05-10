package ru.kuzmin.gptbot.service;

import java.util.concurrent.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.kuzmin.gptbot.enums.GPTModelName;
import ru.kuzmin.gptbot.bot.AbstractKzmGptBot;
import ru.kuzmin.gptbot.exceptions.KzmGptException;
import ru.kuzmin.gptbot.interaction.ChatResponse;

import static ru.kuzmin.gptbot.enums.Role.ASSISTANT;
import static ru.kuzmin.gptbot.enums.Role.USER;
import static ru.kuzmin.gptbot.enums.GPTModelName.GPT_3_5;
import static ru.kuzmin.gptbot.enums.GPTModelName.GPT_4_TURBO;
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

    @Value("${app.responseTimeOutMinutes}")
    private Integer TIME_OUT;

    private final GptClient gptClient;

    private final ResponseParser responseParser;
    private final ExecutorService typer = Executors.newCachedThreadPool();
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public void process(AbstractKzmGptBot bot, Update update) {

        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        String userId = update.getMessage().getFrom().getId().toString();
        String text = update.getMessage().getText();
        String chatId = update.getMessage().getChatId().toString();

        bot.initCache(chatId);

        if (!bot.checkIfUserEnabled(userId)) {
            register(bot, userId, chatId, text);
            return;
        }

        switch (text) {
        case START -> handleStart(bot, chatId);
        case GPT_3_5_MSG -> switchToModel(bot, chatId, GPT_3_5);
        case GPT_4_TURBO_MSG -> switchToModel(bot, chatId, GPT_4_TURBO);
        case FLUSH -> handleFlush(bot, chatId);
        case BALANCE -> handleBalance(bot, chatId);
        case HELP -> sendHelpMessage(bot, chatId);
        default -> processMessage(bot, chatId, text);
        }
    }

    private void processMessage(AbstractKzmGptBot bot, String chatId, String text) {

        Future<?> typingFuture = pretendTyping(bot, chatId);

        bot.addMessageToContext(chatId, USER, text);

        Future<ChatResponse> responseFuture = executor.submit(() ->
             gptClient.getResponse(bot.getCurrentContext(chatId), bot.getCurrentModel(chatId), bot.getTemperature()));

        try {
            ChatResponse response = responseFuture.get(TIME_OUT, TimeUnit.MINUTES);
            String responseMessage = responseParser.getMessage(response);
            bot.addMessageToContext(chatId, ASSISTANT, responseMessage);
            bot.sendMessage(chatId, addPriceToResponseMessage(responseMessage, responseParser.getPrice(response)));
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
        } catch (Exception e) {
            log.error("Exception while processing response", e);
            sendErrorMessage(bot, chatId, "Exception while processing response");
        } finally {
            typingFuture.cancel(true);
            switchToDefaultModel(bot, chatId);
        }
    }

    private Future<?> pretendTyping(AbstractKzmGptBot bot, String chatId) {
        return typer.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                bot.pretendTyping(chatId);
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    private void register(AbstractKzmGptBot bot, String userId, String chatId, String password) {
        if (!password.equals(bot.getPassword())) {
            sendRegisterMessage(bot, chatId);
            return;
        }
        bot.enableUser(userId);
        bot.sendMessage(chatId, "Теперь можно пользоваться ботом!");
        sendHelpMessage(bot, chatId);
    }

    private void handleFlush(AbstractKzmGptBot bot, String chatId) {
        bot.flushContext(chatId);
        bot.sendMessage(chatId, buildFlushContextMessage());
    }

    private void switchToModel(AbstractKzmGptBot bot, String chatId, GPTModelName model) {
        bot.sendMessage(chatId, "Установлена модель " + model.getValue());
        bot.switchModel(chatId, model);
    }

    private void switchToDefaultModel(AbstractKzmGptBot bot, String chatId) {
        if (!bot.getCurrentModel(chatId).equals(GPT_3_5)) {
            switchToModel(bot, chatId, GPT_3_5);
        }
    }

    private void handleStart(AbstractKzmGptBot bot, String chatId) {
        bot.sendMessage(chatId, buildStartMessage());
        sendHelpMessage(bot, chatId);
    }

    private void sendHelpMessage(AbstractKzmGptBot bot, String chatId) {
        bot.sendMessage(chatId, String.format(getHelpMessage(), bot.getCurrentModel(chatId).getValue()));
    }

    private void sendErrorMessage(AbstractKzmGptBot bot, String chatId, String message) {
        bot.sendMessage(chatId, "Exception while trying to get response : " + message);
    }

    private void sendRegisterMessage(AbstractKzmGptBot bot, String chatId) {
        bot.sendMessage(chatId, buildRegisterMessage());
    }

    private void handleBalance(AbstractKzmGptBot bot, String chatId) {
        bot.sendMessage(chatId, buildBalanceMessage(gptClient.getBalance()));
    }

    private String buildBalanceMessage(double balance) {
        return String.format("Текущий баланс: %.3f руб.", balance);
    }

    private String addPriceToResponseMessage(String message, Double price) {
        String priceMessage = String.format("""
                
                ***********
                Приблизительная стоимость запроса %.3f руб.
                ***********
                """, price);
        return message.concat(priceMessage);
    }

    private String buildRegisterMessage() {
        return "Для использования бота необходимо ввести пароль";
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
