package ru.twentyoneh.embedderservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.twentyoneh.embedderservice.dto.embedding.CompareVectorsResponse;
import ru.twentyoneh.embedderservice.dto.embedding.EmbeddingResponse;
import ru.twentyoneh.embedderservice.dto.embedding.VectorMatch;
import ru.twentyoneh.embedderservice.service.EmbeddingService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/embeddings")
public class EmbeddingController {

    private final EmbeddingService embeddingService;

    public EmbeddingController(EmbeddingService embeddingService) {
        this.embeddingService = embeddingService;
    }

    // Пример: GET /api/embeddings/vector?text=Привет, мир
    @GetMapping(path = "/vector", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EmbeddingResponse> embedVector(@RequestParam String text) {
        var result = embeddingService.embed(text);
        return ResponseEntity.ok(result);
    }

//  POST /api/embeddings/compare — сравнить два вектора (косинусное сходство)
    @PostMapping(path = "/compare", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CompareVectorsResponse>> compare(@RequestBody float[] request) {
        List<CompareVectorsResponse> result = embeddingService.compare(request);
        return ResponseEntity.ok(result);
    }
}
