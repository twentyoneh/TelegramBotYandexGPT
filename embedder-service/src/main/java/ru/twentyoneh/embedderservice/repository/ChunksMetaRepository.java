package ru.twentyoneh.embedderservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.twentyoneh.embedderservice.models.ChunksMeta;

public interface ChunksMetaRepository extends JpaRepository<ChunksMeta, Integer> {
}
