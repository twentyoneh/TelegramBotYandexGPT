package ru.twentyoneh.aiorchestrator.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "embedder-auth", url = "${app.embedder.base-url}")
public interface EmbedderAuthClient {

    @PostMapping("${app.embedder.auth.login-path}")
    TokenResponse login(@RequestBody LoginRequest req);

    record LoginRequest(String username, String password) {}
    record TokenResponse(String token) {}
}
