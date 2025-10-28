package ru.twentyoneh.aiorchestrator.feign;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.twentyoneh.aiorchestrator.config.EmbedderFeingConfig;
import ru.twentyoneh.aiorchestrator.dto.CompareVectorsResponse;
import ru.twentyoneh.aiorchestrator.dto.VectorMatch;

import java.util.List;

@FeignClient(name = "embedder-feign-client", url = "${app.embedder.base-url}", configuration = EmbedderFeingConfig.class)
public interface EmbedderFeingClient {
    @GetMapping("/api/embeddings/vector")
    ResponseEntity<JsonNode> getVector(@RequestParam("text") String text);

    @PostMapping("/api/embeddings/compare")
    ResponseEntity<List<CompareVectorsResponse>> compareVectors(@RequestBody float[] request);
}
