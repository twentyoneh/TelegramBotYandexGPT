package ru.twentyoneh.aiorchestrator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain security(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable()) // иначе POST может ловить 403
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(auth -> auth
                                // свободный доступ из tgbot:
                                .requestMatchers(HttpMethod.POST, "/api/chat", "/api/chat/**").permitAll()
                                // если есть другие публичные ручки — добавь их тут:
                                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                                // всё остальное как нужно тебе:
                                .anyRequest().permitAll()     // <- если вообще не нужна авторизация в сервисе
                        // .anyRequest().authenticated() // <- если остальное хочешь оставить закрытым
                )
                .build();
    }
}
