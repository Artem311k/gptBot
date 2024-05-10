package ru.kuzmin.gptbot.exceptions;

/**
 * @author Kuzmin Artem
 * @since 10 май 2024 г.
 */
public class KzmGptException extends RuntimeException {

    public KzmGptException(String message, Throwable cause) {
        super(message, cause);
    }

    public KzmGptException(String message) {
        super(message);
    }
}
