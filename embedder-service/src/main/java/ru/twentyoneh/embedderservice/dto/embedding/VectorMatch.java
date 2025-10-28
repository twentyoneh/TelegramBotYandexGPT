package ru.twentyoneh.embedderservice.dto.embedding;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class VectorMatch {
    private Integer id;
    private HashMap<Integer, Double> id_to_distance; // chunk_id -> distance
}
