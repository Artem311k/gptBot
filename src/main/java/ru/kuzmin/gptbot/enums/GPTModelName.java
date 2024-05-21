package ru.kuzmin.gptbot.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Kuzmin Artem
 * @since 11 апр. 2024 г.
 */
public enum GPTModelName {

    GPT_3_5("gpt-3.5-turbo"),
    GPT_4_O("gpt-4o");

    private final String model;

    GPTModelName(String model) {
        this.model = model;
    }

    @JsonValue
    public String getValue() {
        return model;
    }
}
