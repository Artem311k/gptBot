package ru.kuzmin.gptbot.interaction;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.kuzmin.gptbot.Enum.Role;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    private Role role;
    private String content;

}