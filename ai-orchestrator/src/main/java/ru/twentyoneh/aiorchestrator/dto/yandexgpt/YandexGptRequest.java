package ru.twentyoneh.aiorchestrator.dto.yandexgpt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YandexGptRequest {
    private String modelUri;
    private CompletionOptions completionOptions;
    private List<Message> messages;
}