package ru.kuzmin.gptbot.Enum;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author Kuzmin Artem
 * @since 11 апр. 2024 г.
 */
public enum Role {
    SYSTEM("system"),
    USER("user"),
    ASSISTANT("assistant");

    private final String role;


    Role(String role) {
        this.role = role;
    }

    @JsonValue
    public String getValue() {
        return role;
    }
}

