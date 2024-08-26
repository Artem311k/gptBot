package ru.kuzmin.gptbot.bot;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

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
        if (StringUtils.isNotBlank(prompt)) {
            messages.add(new Message(Role.SYSTEM, prompt));
        }
    }

    public void addMessageToContext(Role role, String text) {
        if (messages.size() >= maxContentLength) {
            removeOldestMessage();
        }
        messages.add(new Message(role, text));
    }

    private void removeOldestMessage() {
        if (messages.isEmpty()) {
            return;
        }
        int indexToRemove = messages.get(0).getRole().equals(Role.SYSTEM) ? 1 : 0;
        if (indexToRemove < messages.size()) {
            messages.remove(indexToRemove);
        }
    }

    public void flushContext() {
        messages = new ArrayList<>();
        messages.add(new Message(Role.SYSTEM, prompt));
    }


}
