package ru.kuzmin.gptbot.service;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.kuzmin.gptbot.enums.GPTModelName;
import ru.kuzmin.gptbot.exceptions.KzmGptException;
import ru.kuzmin.gptbot.interaction.ChatResponse;
import ru.kuzmin.gptbot.interaction.Message;
import ru.kuzmin.gptbot.interaction.Request;
import ru.kuzmin.gptbot.interaction.Balance;

import static ru.kuzmin.gptbot.interaction.Request.newRequest;

/**
 * @author Kuzmin Artem
 * @since 10 апр. 2024 г.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class GptClient {

    private final RestTemplate restTemplate;

    private final TokenStorage tokenStorage;

    @Value("${app.completions.uri}")
    private String completionsUri;

    @Value("${app.balance.uri}")
    private String balanceUri;

    public ChatResponse getResponse(List<Message> context, GPTModelName model, Double temperature) {

        HttpEntity<Request> requestHttpEntity = new HttpEntity<>(
                newRequest(model, context, temperature),
                buildHeaders(tokenStorage.getToken()));

        ResponseEntity<ChatResponse> response = restTemplate.exchange(completionsUri, HttpMethod.POST, requestHttpEntity, ChatResponse.class);

        return Optional.ofNullable(response.getBody())
                .orElseThrow(() -> new KzmGptException(String.format("Returned null from request to [%s[", completionsUri)));
    }

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + token);
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    public double getBalance() {
        HttpEntity<Void> entity = new HttpEntity<>(buildHeaders(tokenStorage.getToken()));
        ResponseEntity<Balance> response = restTemplate.exchange(balanceUri, HttpMethod.GET, entity, Balance.class);
        return Optional.ofNullable(response.getBody())
                .map(Balance::getBalance)
                .orElseThrow(() -> new KzmGptException(String.format("Returned null from balance request [%s]", balanceUri)));
    }

    @PostConstruct
    private void init() {
        restTemplate.getForEntity("https://api.telegram.org/bot" + System.getProperty("botToken") + "/getUpdates?offset=-1", String.class);

    }





}