package ru.twentyoneh.embedderservice.repository;

import ru.twentyoneh.embedderservice.dto.embedding.VectorMatch;

import java.util.List;
import java.util.Optional;

public interface ChunkVectorRepositoryCustom {
    public Optional<VectorMatch> findNearestL2(float[] queryVector);
    public List<VectorMatch> findTopKL2(float[] queryVector, int k);
}
