package ru.twentyoneh.tgbotgateway.config;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Slf4j
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "bot")
public class BotConfig {
    private String name;
    private String token;

    @PostConstruct
    void logConfig() {
        log.info("Bot config loaded: name='{}', token='{}'", name, token);
    }
}
