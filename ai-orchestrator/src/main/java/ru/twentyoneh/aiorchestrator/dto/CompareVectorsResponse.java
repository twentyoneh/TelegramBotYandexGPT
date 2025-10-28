package ru.twentyoneh.aiorchestrator.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CompareVectorsResponse {

    private String chunkText;

    private String urlDoc;

    Integer pageNum;

    Double distance;
}
