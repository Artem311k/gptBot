package ru.kuzmin.gptbot.enums;

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

