package ru.twentyoneh.embedderservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.twentyoneh.embedderservice.models.ChunksMeta;
import ru.twentyoneh.embedderservice.service.MaterialService;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Администрирование материалов", description = "CRUD операции над текстовыми чанками и их эмбеддингами")
public class AdminController {

    private final MaterialService materialService;

    public AdminController(MaterialService materialService) {
        this.materialService = materialService;
    }


    @Operation(
            summary = "Список всех материалов",
            description = "Возвращает все записи из таблицы ds.chunks_meta. Эмбеддинги напрямую не возвращаются."
    )
    @ApiResponse(responseCode = "200", description = "Успешно", content = @Content(
            mediaType = MediaType.APPLICATION_JSON_VALUE,
            schema = @Schema(implementation = ChunksMeta.class)))
    @GetMapping(path = "/materials", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Iterable<ChunksMeta>> getAllMaterials() {
        log.info("get all materials");
        return materialService.getAllMaterials();
    }

    @Operation(
            summary = "Получить материал по ID",
            description = "Возвращает метаданные чанка по его идентификатору."
    )
    @ApiResponse(responseCode = "200", description = "Найден")
    @ApiResponse(responseCode = "404", description = "Не найден")
    @GetMapping(path = "/materials/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChunksMeta> getMaterialById(@PathVariable Integer id) {
        log.info("get material by id {}", id);
        return materialService.getMaterialById(id);
    }

    @Operation(
            summary = "Создать новый материал",
            description = "Создаёт запись и инициирует генерацию векторного представления текста."
    )
    @ApiResponse(responseCode = "201", description = "Создан")
    @ApiResponse(responseCode = "400", description = "Некорректные данные")
    @PostMapping(path = "/materials", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChunksMeta> addMaterial(@RequestBody ChunksMeta material) {
        log.info("add material {}", material);
        return materialService.addMaterial(material);
    }

    @Operation(
            summary = "Обновить материал",
            description = "Обновляет поля; если изменён текст чанка — пересчитывается эмбеддинг."
    )
    @ApiResponse(responseCode = "200", description = "Обновлён")
    @ApiResponse(responseCode = "404", description = "Не найден")
    @PutMapping(path = "/materials/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ChunksMeta> updateMaterial(@PathVariable Integer id, @RequestBody ChunksMeta material) {
        log.info("update material {}", material);
        return materialService.updateMaterial(id, material);
    }

    @Operation(
            summary = "Удалить материал",
            description = "Удаляет запись и связанный вектор (через ON DELETE CASCADE)."
    )
    @ApiResponse(responseCode = "204", description = "Удалён")
    @ApiResponse(responseCode = "404", description = "Не найден")
    @DeleteMapping("/material/{id}")
    public ResponseEntity<Void> deleteMaterial(@PathVariable Integer id) {
        log.info("delete material {}", id);
        return materialService.deleteMaterial(id);
    }

}
