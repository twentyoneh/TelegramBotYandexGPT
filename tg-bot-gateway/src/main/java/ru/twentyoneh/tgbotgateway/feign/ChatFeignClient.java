package ru.twentyoneh.tgbotgateway.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import ru.twentyoneh.tgbotgateway.config.ChatFeignConfig;
import ru.twentyoneh.tgbotgateway.dto.ChatRequestDto;

@FeignClient(name = "chat-feign-client", url = "${app.chat.base-url}", configuration = ChatFeignConfig.class)
public interface ChatFeignClient {

    @PostMapping("/api/chat")
    ResponseEntity<String> sendMessage(@RequestBody ChatRequestDto userInput);
}
