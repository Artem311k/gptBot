package ru.kuzmin.gptbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import lombok.extern.slf4j.Slf4j;
import ru.kuzmin.gptbot.service.GptService;

@SpringBootApplication
@Slf4j
public class GptBotApplication {

    public static void main(String[] args) {
        log.info("bot token is {}", System.getProperty("botToken"));
        log.info("api token is {}", System.getProperty("apiToken"));

       SpringApplication.run(GptBotApplication.class, args);

    }

}
