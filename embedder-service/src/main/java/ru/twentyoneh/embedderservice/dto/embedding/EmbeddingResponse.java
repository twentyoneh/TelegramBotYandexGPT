package ru.twentyoneh.embedderservice.dto.embedding;

import lombok.Data;

import java.util.List;

@Data
public class EmbeddingResponse {
    private float[] embedding;
}
