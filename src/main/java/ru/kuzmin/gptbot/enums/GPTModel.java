package ru.kuzmin.gptbot.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Kuzmin Artem
 * @since 11 апр. 2024 г.
 */
public enum GPTModel {

    GPT_3_5("gpt-3.5-turbo"),
    GPT_4_TURBO("gpt-4-turbo-preview");

    private final String model;

    GPTModel(String model) {
        this.model = model;
    }

    @JsonValue
    public String getValue() {
        return model;
    }
}
