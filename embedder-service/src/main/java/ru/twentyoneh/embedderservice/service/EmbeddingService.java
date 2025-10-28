package ru.twentyoneh.embedderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pgvector.PGvector;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import ru.twentyoneh.embedderservice.config.YandexEmbProperties;
import ru.twentyoneh.embedderservice.dto.embedding.CompareVectorsResponse;
import ru.twentyoneh.embedderservice.dto.embedding.EmbeddingRequest;
import ru.twentyoneh.embedderservice.dto.embedding.EmbeddingResponse;
import ru.twentyoneh.embedderservice.dto.embedding.VectorMatch;
import ru.twentyoneh.embedderservice.models.ChunkVector;
import ru.twentyoneh.embedderservice.models.ChunksMeta;
import ru.twentyoneh.embedderservice.repository.ChunkVectorRepository;
import ru.twentyoneh.embedderservice.repository.ChunksMetaRepository;
import ru.twentyoneh.embedderservice.utils.WebClientLoggingFilters;

import java.time.Duration;
import java.util.*;

@Slf4j
@Service
public class EmbeddingService {
    private final WebClient client;
    private final YandexEmbProperties props;
    private final ChunkVectorRepository chunkVectorRepository;
    private final ChunksMetaRepository chunksMetaRepository;


    public EmbeddingService(WebClient.Builder builder, YandexEmbProperties props, ChunkVectorRepository chunkVectorRepository, ChunksMetaRepository chunksMetaRepository ) {
        this.client = builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Api-Key " + props.getApiKey())
                .defaultHeader("x-folder-id", props.getFolderId())
//                .filter(WebClientLoggingFilters.logRequest())
//                .filter(WebClientLoggingFilters.logResponse())
                .build();
        this.props = props;
        this.chunkVectorRepository = chunkVectorRepository;
        this.chunksMetaRepository = chunksMetaRepository;
    }


    public EmbeddingResponse embed(String text) {
        String modelUri = "emb://" + props.getFolderId() + "/text-search-query";
        EmbeddingRequest request = new EmbeddingRequest(modelUri, text,256);


        log.info("Embedding request: {}", request);

        var response =  client.post()
                .uri("/foundationModels/v1/textEmbedding")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Yandex API error " + resp.statusCode() + ": " + body))
                )
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));

        float[] embArray = new float[0];
        if (response != null) {
            JsonNode emb = response.get("embedding");
            if (emb != null && emb.isArray()) {
                int size = emb.size();
                embArray = new float[size];
                int i = 0;
                for (JsonNode n : emb) {
                    embArray[i++] = (float) n.asDouble();
                }
            }
        }

        EmbeddingResponse embeddingResponse = new EmbeddingResponse();
        embeddingResponse.setEmbedding(embArray);

        return  embeddingResponse;
    }

    public List<CompareVectorsResponse> compare(float[] queryVector) {
        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("Вектор пуст");
        }
        int topK = 3;

        List<VectorMatch> matches = chunkVectorRepository.findTopKL2(queryVector, topK);

        if(matches.isEmpty()){
            log.info("Совпадений не найдено");
            return null;
        }

        List<CompareVectorsResponse> respones = new ArrayList<>();
        matches.forEach(vm -> {
            log.info("Найден match: {}", vm);

            Map<Integer, Double> idToDistance = vm.getId_to_distance();
            if (idToDistance != null && !idToDistance.isEmpty()) {
                Map.Entry<Integer, Double> first = idToDistance.entrySet().iterator().next();
                Integer materialId = first.getKey();
                Double distance = first.getValue();
                log.info("materialId={}, distance={}", materialId, distance);
                ChunksMeta meta = chunksMetaRepository.findById(materialId).orElse(null);
                respones.add(CompareVectorsResponse.builder()
                                .distance(distance)
                                .chunkText(meta.getChunkText())
                                .urlDoc(meta.getUrlDoc())
                                .pageNum(meta.getPageNum())
                        .build());
                log.info("Мета: {}", meta);
            }

        });

        return respones;
    }

    public void addEmbeddingForChunk(ChunksMeta chunk) {
        if (chunkVectorRepository.existsByChunk_Id(chunk.getId())) {
            log.info("Вектор уже существует для chunk id={}, выполняю update", chunk.getId());
            updateVectorForChunk(chunk);
            return;
        }
        float[] vector = embed(chunk.getChunkText()).getEmbedding();
        ChunkVector cv = ChunkVector.builder()
                .chunk(chunk)
                .embedding(vector)
                .build();
        chunkVectorRepository.save(cv);
        log.info("Создан вектор chunk id={} (len={})", chunk.getId(), vector.length);
    }

    @Transactional
    public void updateVectorForChunk(ChunksMeta chunk) {
        float[] vector = embed(chunk.getChunkText()).getEmbedding();
        ChunkVector cv = chunkVectorRepository.findByChunk_Id(chunk.getId())
                .orElseGet(() -> ChunkVector.builder()
                        .chunk(chunk)
                        .build());
        cv.setEmbedding(vector);
        chunkVectorRepository.save(cv);
        log.info("Обновлён вектор chunk id={} (len={})", chunk.getId(), vector.length);
    }

    @Transactional
    public void deleteVectorForChunk(Integer chunkId) {
        if (chunkVectorRepository.existsByChunk_Id(chunkId)) {
            chunkVectorRepository.deleteByChunk_Id(chunkId);
            log.info("Удалён вектор chunk id={}", chunkId);
        } else {
            log.debug("Вектор отсутствует chunk id={}", chunkId);
        }
    }

}
