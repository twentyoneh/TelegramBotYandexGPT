package ru.twentyoneh.embedderservice.dto.embedding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbeddingRequest {
    private String modelUri; // emb://<folderId>/text-search-doc/latest
    private String text;
    private Integer dim;
}
