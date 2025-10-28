package ru.twentyoneh.tgbotgateway.service;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import feign.RetryableException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.twentyoneh.tgbotgateway.dto.ChatRequestDto;
import ru.twentyoneh.tgbotgateway.feign.ChatFeignClient;
import ru.twentyoneh.tgbotgateway.utils.Sender;

@Slf4j
@RequiredArgsConstructor
@Component
public class QuestionHandler {

    private final ChatFeignClient chatFeignClient;


    public SendMessage handle(Update update) {
        log.info("Handling question: " + update.getMessage().getText());
        Long chatId = update.getMessage().getChatId();
        String question = update.getMessage().getText();
        ChatRequestDto chatRequestDto = ChatRequestDto.builder()
                .sustemMessage("Ты - ассистент студента онлайн школы Noeflex. Отвечай на вопросы максимально подробно и развернуто. Если не знаешь ответа, скажи что не знаешь, но постарайся помочь.")
                .userMessage(question)
                .build();


        String reply;
        try {
            ResponseEntity<String> response = chatFeignClient.sendMessage(chatRequestDto);
            if (response == null) {
                log.error("Feign вернул null ResponseEntity");
                reply = "Не удалось получить ответ. Попробуйте позже.";
            } else if (!response.getStatusCode().is2xxSuccessful()) {
                log.warn("Чат-сервис ответил статусом {}: {}", response.getStatusCode(), response.getBody());
                reply = "Сервис временно отвечает с ошибкой (" + response.getStatusCode().value() + "). Попробуйте позже.";
            } else {
                reply = response.getBody() != null ? response.getBody() : "Получен пустой ответ.";
            }
        } catch (RetryableException e) {
            log.error("Чат-сервис недоступен (RetryableException)", e);
            reply = "Сервис чата недоступен. Повторите позже.";
        } catch (FeignException e) {
            log.error("Feign ошибка при вызове чат-сервиса: status={}, content={}",
                    e.status(), safeBody(e));
            if(e.status() == 503){
                reply = "База данных временно недоступна. Попробуйте позже.";
            } else {
                reply = "Ошибка при обращении к чат-сервису (" + e.status() + ").";
            }
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обработке вопроса", e);
            reply = "Произошла внутренняя ошибка. Попробуйте позже.";
        }

        return Sender.sendMessage(chatId, reply);
    }

    private String safeBody(FeignException e) {
        try {
            return e.contentUTF8();
        } catch (Exception ignored) {
            return "<unavailable>";
        }
    }
}
