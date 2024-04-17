package ru.kuzmin.gptbot.interaction;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */

@Getter
@Setter
public class ChatResponse {

    private List<Choice> choices;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Choice {

        private int index;
        private Message message;

    }
}
