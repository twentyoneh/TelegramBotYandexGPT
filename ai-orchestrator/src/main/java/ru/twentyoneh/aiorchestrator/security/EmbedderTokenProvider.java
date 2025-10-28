package ru.twentyoneh.aiorchestrator.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.twentyoneh.aiorchestrator.feign.EmbedderAuthClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class EmbedderTokenProvider {
    private final EmbedderAuthClient authClient;
    private final ObjectMapper om = new ObjectMapper();

    @Value("${app.embedder.auth.username}") private String username;
    @Value("${app.embedder.auth.password}") private String password;
    @Value("${app.embedder.auth.cache-seconds:0}") private long fallbackCacheSec;

    private volatile String token;
    private volatile long expiresAtEpochSec; // по exp из JWT

    public synchronized String getToken() {
        long now = System.currentTimeMillis() / 1000;
        if (token == null || now >= (expiresAtEpochSec - 30)) {
            var resp = authClient.login(new EmbedderAuthClient.LoginRequest(username, password));
            this.token = resp.token();
            this.expiresAtEpochSec = parseExpOrFallback(this.token, now, fallbackCacheSec);
        }
        return token;
    }

    private long parseExpOrFallback(String jwt, long now, long fallback) {
        try {
            String[] parts = jwt.split("\\.");
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            JsonNode n = om.readTree(payloadJson);
            if (n.has("exp")) return n.get("exp").asLong();
        } catch (Exception ignored) {}
        return now + Math.max(fallback, 300);
    }
}
