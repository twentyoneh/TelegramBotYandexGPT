package ru.twentyoneh.embedderservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "yandex.gpt")
public class YandexEmbProperties {
    private String apiKey;
    private String folderId;
    private String baseUrl;
}



