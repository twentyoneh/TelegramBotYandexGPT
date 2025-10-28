package ru.twentyoneh.aiorchestrator.dto.yandexgpt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompletionOptions {
    private boolean stream;
    private double temperature;
    private Integer maxTokens;
    private ReasoningOptions reasoningOptions;
}