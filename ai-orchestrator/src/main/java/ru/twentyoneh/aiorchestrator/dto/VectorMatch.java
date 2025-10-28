package ru.twentyoneh.aiorchestrator.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class VectorMatch {
    private Integer id;
    private Map<Integer, Double> id_to_distance;
}

