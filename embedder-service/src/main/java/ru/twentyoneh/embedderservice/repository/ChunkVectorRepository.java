package ru.twentyoneh.embedderservice.repository;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import ru.twentyoneh.embedderservice.models.ChunkVector;

import java.util.Optional;

public interface ChunkVectorRepository extends JpaRepository<ChunkVector, Integer>, ChunkVectorRepositoryCustom {
    Optional<ChunkVector> findByChunk_Id(Integer chunkId);

    @Transactional
    @Modifying
    void deleteByChunk_Id(Integer chunkId);

    boolean existsByChunk_Id(Integer chunkId);
}
