package ru.kuzmin.gptbot.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author Kuzmin Artem
 * @since 26 авг. 2024 г.
 */

@AllArgsConstructor
@Getter
public enum ErrorStatuses {

    AUTH(401, "Неверная аутентификация или Предоставлен неверный ключ API"),
    LIMIT(429, "Достигнут лимит запросов или Вы превысили вашу текущу"),
    ERR(500, "Сервер столкнулся с ошибкой при обработке вашего запроса, пожалуйста, попробуйте позже"),
    OVERLOAD(503, " Движок в настоящее время перегружен, пожалуйста, попробуйте позже");


    private final int code;
    private final String msg;
}
