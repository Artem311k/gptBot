package ru.kuzmin.gptbot.utils;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import ru.kuzmin.gptbot.enums.Role;
import ru.kuzmin.gptbot.interaction.Message;

/**
 * @author Kuzmin Artem
 * @since 18 апр. 2024 г.
 */

@Getter
public class ChatContext {

    private List<Message> messages = new ArrayList<>();

    private final int maxContentLength;

    private final String prompt;

    public ChatContext(int maxContentLength, String prompt) {
        this.maxContentLength = maxContentLength;
        this.prompt = prompt;
        messages.add(new Message(Role.SYSTEM, prompt));
    }

    public void addMessageToContext(Role role, String text){
        if (messages.size() >= maxContentLength) {
            messages.remove(1);
        }
        messages.add(new Message(role, text));
    }

    public void flushContext() {
        messages = new ArrayList<>();
        messages.add(new Message(Role.SYSTEM, prompt));
    }


}
