package ru.kuzmin.gptbot.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import ru.kuzmin.gptbot.Enum.GPTModel;
import ru.kuzmin.gptbot.interaction.ChatResponse;
import ru.kuzmin.gptbot.interaction.Message;
import ru.kuzmin.gptbot.interaction.Request;

import static ru.kuzmin.gptbot.interaction.Request.newRequest;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */

@Service
@RequiredArgsConstructor
public class GptClient {

    private final RestTemplate restTemplate;


    @Value("${app.completions.uri}")
    private String completionsUri;

    public String getResponse(List<Message> messages, GPTModel model, Double temperature, String apiToken) {

        HttpEntity<Request> requestHttpEntity = new HttpEntity<>(
                newRequest(model, messages, temperature),
                buildHeaders(apiToken));

        ResponseEntity<ChatResponse> response = restTemplate.exchange(completionsUri, HttpMethod.POST, requestHttpEntity, ChatResponse.class);

        return Optional.ofNullable(response.getBody())
                .map(r -> r.getChoices().get(0).getMessage().getContent())
                .orElseThrow(() -> new RuntimeException(String.format("Returned null from request to {%s}", completionsUri)));
    }


    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @PostConstruct
    private void init() {
        restTemplate.getForEntity("https://api.telegram.org/bot" + System.getProperty("botToken") + "/getUpdates?offset=-1", String.class);

    }





}