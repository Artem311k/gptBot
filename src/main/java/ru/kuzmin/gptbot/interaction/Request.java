package ru.kuzmin.gptbot.interaction;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kuzmin.gptbot.Enum.GPTModel;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Request {

    private GPTModel model;
    private List<Message> messages;
    private Double temperature;

    public static Request newRequest(GPTModel model, List<Message> messages, Double temperature) {
        return new Request(model, messages, temperature);

    }
}
