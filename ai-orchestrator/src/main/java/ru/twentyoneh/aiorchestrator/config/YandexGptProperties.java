package ru.twentyoneh.aiorchestrator.config;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yandex.gpt")
public class YandexGptProperties {
    private String apiKey;
    private String folderId;
    private String baseUrl;
}
