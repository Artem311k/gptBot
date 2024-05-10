package ru.kuzmin.gptbot.interaction;

import java.util.List;

import lombok.*;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */

@Getter
@Setter
@ToString
public class ChatResponse {

    private List<Choice> choices;
    private Usage usage;
    private String model;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @ToString
    public static class Choice {

        private int index;
        private Message message;

    }
}
