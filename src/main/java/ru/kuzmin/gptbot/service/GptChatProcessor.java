package ru.kuzmin.gptbot.service;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Update;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.kuzmin.gptbot.bot.AbstractKzmGptBot;
import ru.kuzmin.gptbot.interaction.ChatResponse;

import static ru.kuzmin.gptbot.enums.Role.ASSISTANT;
import static ru.kuzmin.gptbot.enums.Role.USER;
import static ru.kuzmin.gptbot.utils.Commands.*;

/**
 * @author Kuzmin Artem
 * @since 18 апр. 2024 г.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class GptChatProcessor {

    @Value("${app.responseTimeOutMinutes}")
    private Integer TIME_OUT;

    private final GptClient gptClient;

    private final ResponseParser responseParser;
    private final ExecutorService executor = Executors.newFixedThreadPool(50, new ThreadFactory() {
        private final AtomicInteger threadNumber = new AtomicInteger(1);

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "executor-thread-" + threadNumber.incrementAndGet());
        }
    });

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
        case FLUSH -> handleFlush(bot, chatId);
        case BALANCE -> handleBalance(bot, chatId);
        case HELP -> sendHelpMessage(bot, chatId);
        case DEFAULT_PROMPT -> handeSwitchPrompt(bot, chatId);
        default -> processMessage(bot, chatId, text);
        }
    }

    private void processMessage(AbstractKzmGptBot bot, String chatId, String text) {

        Future<?> typingFuture = pretendTyping(bot, chatId);

        bot.addMessageToContext(chatId, USER, text);

        Future<ChatResponse> responseFuture = executor.submit(() ->
             gptClient.getResponse(bot.getCurrentContext(chatId),
                     bot.getCurrentModel(chatId),
                     bot.getTemperature()));

        try {
            ChatResponse response = responseFuture.get(TIME_OUT, TimeUnit.MINUTES);
            String responseMessage = responseParser.getMessage(response);
            bot.addMessageToContext(chatId, ASSISTANT, responseMessage);
            bot.sendMessage(chatId, addPriceToResponseMessage(responseMessage, responseParser.getPrice(response), response.getModel()));
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
            log.error("Exception while processing response.", e);
            sendErrorMessage(bot, chatId, e.getMessage());
        } finally {
            typingFuture.cancel(true);
//            switchToDefaultModel(bot, chatId);
        }
    }

    private Future<?> pretendTyping(AbstractKzmGptBot bot, String chatId) {
        return executor.submit(() -> {
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
        if (StringUtils.isNotBlank(bot.getPassword()) && !password.equals(bot.getPassword())) {
            sendRegisterMessage(bot, chatId);
            return;
        }
        bot.enableUser(userId);
        sendHelpMessage(bot, chatId);
    }

    private void handeSwitchPrompt(AbstractKzmGptBot bot, String chatId) {
        boolean changed = switchDefaultPrompt(bot,chatId);
        if (changed) {
            bot.sendMessage(chatId, "Установлен промпт по умолчанию \"" + bot.getDefaultPrompt() +  "\".");
            return;
        }
        bot.sendMessage(chatId, "Промпт по умолчанию выключен");
    }

    private boolean switchDefaultPrompt(AbstractKzmGptBot bot, String chatId) {
        return bot.switchDefaultPrompt(chatId);
    }

    private void handleFlush(AbstractKzmGptBot bot, String chatId) {
        bot.flushContext(chatId);
        bot.sendMessage(chatId, buildFlushContextMessage());
    }

    private void handleStart(AbstractKzmGptBot bot, String chatId) {
        bot.sendMessage(chatId, buildStartMessage());
    }

    private void sendHelpMessage(AbstractKzmGptBot bot, String chatId) {
        bot.sendMessage(chatId, String.format(
                getHelpMessage(),
                bot.getCurrentModel(chatId).getValue(),
                bot.isUseDefaultPrompt() ? bot.getDefaultPrompt() : "Промпт по умолчанию не задан или выключен",
                gptClient.getBalance()));
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
        return String.format("Текущий баланс: %.2f руб.", balance);
    }

    private String addPriceToResponseMessage(String message, Double price, String model) {
        String priceMessage = String.format("""
                
                ***********
                Приблизительная стоимость запроса %.2f руб.
                Модель %s
                ***********
                """, price, model);
        return message.concat(priceMessage);
    }

    private String buildRegisterMessage() {
        return "Для использования бота необходимо ввести пароль";
    }

    private String getHelpMessage() {
        return """
                Текущая модель %s.
                Промпт: %s.
                /defprompt - вкл/выкл промпт по умолчанию (Отвечать коротко на каждый вопрос)
                /balance - баланс (%.2f руб.)
                /flush - Сборс контекста
                /help - Текущая информация
                """;
    }

    private String buildStartMessage() {
        return "Спросите что-нибудь у бота.";
    }

    private String buildFlushContextMessage() {
        return "Контекст сброшен";
    }

}
