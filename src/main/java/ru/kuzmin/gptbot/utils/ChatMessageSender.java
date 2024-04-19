package ru.kuzmin.gptbot.utils;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Kuzmin Artem
 * @since 18 апр. 2024 г.
 */

@Component
@Slf4j
public class ChatMessageSender {

    public void sendTypingAction(TelegramLongPollingBot bot, String chatId) {
        SendChatAction sendChatAction = new SendChatAction();
        sendChatAction.setChatId(chatId);
        sendChatAction.setAction(ActionType.TYPING);
        try {
            bot.executeAsync(sendChatAction);
        } catch (TelegramApiException ex) {
            log.error("Exception while sending message", ex);

        }
    }

    public void sendMessage(TelegramLongPollingBot bot, String chatId, String text) {
        sendMessage(bot, message(chatId, text));
    }

    public void sendMessage(TelegramLongPollingBot bot, SendMessage sendMessage) {
        try {
            bot.executeAsync(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Error sending message to {} with {}", sendMessage.getChatId(), bot.getBotUsername());
        }
    }

    private SendMessage message(String chatId, String text) {
        SendMessage sendMessage = new SendMessage(chatId, text);
        sendMessage.enableMarkdown(true);
        return sendMessage;
    }


}
