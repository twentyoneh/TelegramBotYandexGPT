package ru.twentyoneh.embedderservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.twentyoneh.embedderservice.models.ChunksMeta;
import ru.twentyoneh.embedderservice.repository.ChunkVectorRepository;
import ru.twentyoneh.embedderservice.repository.ChunksMetaRepository;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {
    private final ChunksMetaRepository chunksRepository;
    private final EmbeddingService embeddingService;

    @Transactional
    public ResponseEntity<Iterable<ChunksMeta>> getAllMaterials() {
        log.debug("Запрос на получение всех материалов");
        Iterable<ChunksMeta> all = chunksRepository.findAll();
        log.debug("Найдено материалов: {}", (all instanceof Collection<?> c) ? c.size() : "unknown");
        return ResponseEntity.ok(all);
    }

    @Transactional
    public ResponseEntity<ChunksMeta> getMaterialById(Integer id) {
        log.debug("Запрос материала id={}", id);
        return chunksRepository.findById(id)
                .map(cm -> {
                    log.debug("Материал id={} найден", id);
                    return ResponseEntity.ok(cm);
                })
                .orElseGet(() -> {
                    log.warn("Материал id={} не найден", id);
                    return ResponseEntity.notFound().build();
                });
    }

    public ChunksMeta getMaterialById_data(Integer id) {
        return chunksRepository.findById(id).orElse(null);
    }


    @Transactional
    public ResponseEntity<ChunksMeta> addMaterial(ChunksMeta material) {
        log.debug("Добавление нового материала (предварительный id=null)");
        ChunksMeta saved = chunksRepository.save(material);
        log.debug("Сохранён материал в БД id={}. Генерация эмбеддинга...", saved.getId());
        try {
            embeddingService.addEmbeddingForChunk(saved);
            log.info("Материал id={} создан и эмбеддинг сохранён", saved.getId());
        } catch (Exception e) {
            log.error("Ошибка генерации эмбеддинга для материала id={}", saved.getId(), e);
            // Можно рассмотреть откат или асинхронную повторную попытку
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @Transactional
    public ResponseEntity<ChunksMeta> updateMaterial(Integer id, ChunksMeta material) {
        log.debug("Обновление материала id={}", id);
        Optional<ChunksMeta> opt = chunksRepository.findById(id);
        if (opt.isEmpty()) {
            log.warn("Материал для обновления id={} не найден", id);
            return ResponseEntity.notFound().build();
        }
        material.setId(id);

        try {
            embeddingService.updateVectorForChunk(material);
            log.info("Материал id={} обновлён и эмбеддинг пересоздан", id);
        } catch (Exception e) {
            log.error("Ошибка обновления эмбеддинга для материала id={}", id, e);
        }

        ChunksMeta existing = opt.get();

        existing.setCourseName(material.getCourseName());
        existing.setSectionName(material.getSectionName());
        existing.setChunkText(material.getChunkText());
        existing.setUrlDoc(material.getUrlDoc());
        existing.setTags(material.getTags());
        existing.setPageNum(material.getPageNum());

        ChunksMeta saved = chunksRepository.save(existing);
        log.debug("Материал id={} обновлён в БД.", id);

        return ResponseEntity.ok(saved);
    }

    @Transactional
    public ResponseEntity<Void> deleteMaterial(Integer id) {
        log.debug("Удаление материала id={}", id);
        if (!chunksRepository.existsById(id)) {
            log.warn("Материал для удаления id={} не найден", id);
            return ResponseEntity.notFound().build();
        }
        log.debug("Материал id={} удаление эмбеддинга...", id);
        try {
            embeddingService.deleteVectorForChunk(id);
            log.info("Материал id={} и его эмбеддинг удалены", id);
        } catch (Exception e) {
            log.error("Ошибка удаления эмбеддинга для материала id={}", id, e);
        }
        chunksRepository.deleteById(id);
        log.debug("Материал id={} удалён из БД.", id);
        return ResponseEntity.noContent().build();
    }

}
