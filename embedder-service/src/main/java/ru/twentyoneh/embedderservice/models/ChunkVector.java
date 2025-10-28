package ru.twentyoneh.embedderservice.models;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.*;
import org.hibernate.annotations.*;
import org.hibernate.type.SqlTypes;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "chunk_vectors", schema = "ds")
public class ChunkVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chunk_vectors_pk")
    private Integer id;

    @OneToOne
    @JoinColumn(name = "chunk_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private ChunksMeta chunk;

    @Column(name = "embedding", columnDefinition = "vector(256)")
    @JdbcTypeCode(SqlTypes.VECTOR)
    private float[] embedding;

}
