package ru.twentyoneh.embedderservice.repository;

import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.twentyoneh.embedderservice.dto.embedding.VectorMatch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class ChunkVectorRepositoryImpl implements ChunkVectorRepositoryCustom {

    private final JdbcTemplate jdbcTemplate;
    private static final int EMBEDDING_DIM = 256; // подгони под свою размерность

    @Transactional(readOnly = true)
    public Optional<VectorMatch> findNearestL2(float[] queryVector) {
        List<VectorMatch> top1 = findTopKL2(queryVector, 1);
        return top1.isEmpty() ? Optional.empty() : Optional.of(top1.get(0));
    }

    @Transactional(readOnly = true)
    public List<VectorMatch> findTopKL2(float[] queryVector, int k) {
        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("queryVector пуст");
        }
        if (queryVector.length != EMBEDDING_DIM) {
            throw new IllegalArgumentException("Ожидается вектор длины " + EMBEDDING_DIM +
                    ", а пришёл " + queryVector.length);
        }

        PGvector vec = new PGvector(queryVector);

        String sql = """
            SELECT 
                chunk_vectors_pk AS id,
                chunk_id         AS chunk_id,
                (embedding <-> ?) AS distance   -- Евклидово расстояние
            FROM ds.chunk_vectors
            ORDER BY embedding <-> ?           -- тот же вектор во второй раз
            LIMIT ?
        """;

        return jdbcTemplate.query(
                sql,
                ps -> {
                    ps.setObject(1, vec);
                    ps.setObject(2, vec);
                    ps.setInt(3, k);
                },
                (rs, rowNum) -> {
                    HashMap<Integer, Double> map = new HashMap<>();
                    map.put(rs.getInt("chunk_id"), rs.getDouble("distance"));
                    return new  VectorMatch(rs.getInt("id"), map);
                }
        );
    }
}
