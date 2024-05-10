package ru.kuzmin.gptbot.interaction;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Kuzmin Artem
 * @since 10 май 2024 г.
 */

@Getter
@Setter
@AllArgsConstructor
@ToString
public class Usage {

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;

}
