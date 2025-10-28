package ru.twentyoneh.aiorchestrator.controller;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.twentyoneh.aiorchestrator.client.YandexGptClient;
import ru.twentyoneh.aiorchestrator.dto.ChatRequestDto;

@Slf4j
@RestController
@RequestMapping("/api/chat")
public class ChatController {
    private final YandexGptClient yandexGptClient;

    public ChatController(YandexGptClient yandexGptClient) {
        this.yandexGptClient = yandexGptClient;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> chat(@RequestBody ChatRequestDto request) {
        return yandexGptClient.chat(request);
    }
}
