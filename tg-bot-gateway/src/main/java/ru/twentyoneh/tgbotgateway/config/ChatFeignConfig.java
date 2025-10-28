package ru.twentyoneh.tgbotgateway.config;

import feign.Logger;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "ru.twentyoneh.tgbotgateway.feign")
public class ChatFeignConfig {
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL;
    }

//    @Bean
//    public RequestInterceptor authInterceptor() {
//        return requestTemplate -> requestTemplate.header("Authorization", "Bearer token");
//    }
}
