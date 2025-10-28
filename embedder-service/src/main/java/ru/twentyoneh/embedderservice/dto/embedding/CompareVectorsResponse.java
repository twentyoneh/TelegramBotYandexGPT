package ru.twentyoneh.embedderservice.dto.embedding;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CompareVectorsResponse {

    private String chunkText;

    private String urlDoc;

    Integer pageNum;

    Double distance;
}
