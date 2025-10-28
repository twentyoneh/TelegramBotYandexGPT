package ru.twentyoneh.aiorchestrator.config;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import ru.twentyoneh.aiorchestrator.security.EmbedderTokenProvider;

@Configuration
@EnableFeignClients(basePackages = "ru.twentyoneh.aiorchestrator.feign")
public class EmbedderFeingConfig {

    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

    @Bean
    public RequestInterceptor embedderAuthInterceptor(EmbedderTokenProvider provider) {
        return template -> template.header(
                HttpHeaders.AUTHORIZATION, "Bearer " + provider.getToken()
        );
    }
}
