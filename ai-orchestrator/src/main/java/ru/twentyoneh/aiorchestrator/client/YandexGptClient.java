package ru.twentyoneh.aiorchestrator.client;

import com.fasterxml.jackson.databind.JsonNode;
import feign.FeignException;
import feign.RetryableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.twentyoneh.aiorchestrator.config.YandexGptProperties;
import ru.twentyoneh.aiorchestrator.dto.ChatRequestDto;
import ru.twentyoneh.aiorchestrator.dto.CompareVectorsResponse;
import ru.twentyoneh.aiorchestrator.dto.VectorMatch;
import ru.twentyoneh.aiorchestrator.dto.yandexgpt.*;
import ru.twentyoneh.aiorchestrator.feign.EmbedderFeingClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class YandexGptClient {
    private final WebClient client;
    private final YandexGptProperties props;
    private final EmbedderFeingClient feingClient;

    public YandexGptClient(WebClient.Builder builder, YandexGptProperties props, EmbedderFeingClient feingClient) {
        this.client = builder
                .baseUrl(props.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Api-Key " + props.getApiKey())
                .defaultHeader("x-folder-id", props.getFolderId())
                .build();
        this.props = props;
        this.feingClient = feingClient;
    }

    public ResponseEntity<String> chat(ChatRequestDto userInput) {
        String system = userInput.getSustemMessage();
        String prompt = userInput.getUserMessage();
        log.info("chat start prompt='{}'", prompt);

        // тут должен вызываться embend сервис который будет искать в базе похожие вопросы и добавлять их в system
//        JsonNode body = feingClient.getVector(prompt).getBody();

        ResponseEntity<JsonNode> vectorResponse;
        try {
            vectorResponse = feingClient.getVector(prompt);
        } catch (RetryableException e) {
            log.error("Embedding сервис недоступен (RetryableException)", e);
            return ResponseEntity.status(503).body("Embedding сервис недоступен. Повторите позже.");
        } catch (FeignException e) {
            String body;
            try {
                body = e.contentUTF8();
            } catch (Exception ex) {
                body = "<no-body>";
            }
            log.error("Feign ошибка при получении embedding: status={} body={}", e.status(), body);
            int status = e.status() <= 0 ? 502 : e.status();
            return ResponseEntity.status(status).body("Ошибка embedding сервиса (" + status + "): " + body);
        } catch (Exception e) {
            log.error("Неожиданная ошибка при обращении к embedding сервису", e);
            return ResponseEntity.internalServerError().body("Внутренняя ошибка при получении embedding.");
        }

        if (vectorResponse == null) {
            log.error("Не удалось получить embedding: ответ null");
            return ResponseEntity.status(502).body("Embedding: ответ null");
        }

        if (!vectorResponse.getStatusCode().is2xxSuccessful() || vectorResponse.getBody() == null) {
            String errBody = vectorResponse.getBody() != null ? vectorResponse.getBody().toString() : "body=null";
            log.error("Embedding неуспешен: статус={} body={}", vectorResponse.getStatusCode(), errBody);
            int code = vectorResponse.getStatusCode().value();
            int mapped = (code >= 500) ? code : (code == 404 ? 404 : 502);
            return ResponseEntity.status(mapped)
                    .body("Ошибка получения embedding: статус=" + code + " body=" + errBody);
        }

        JsonNode embeddingNode = vectorResponse.getBody().path("embedding");
        if (embeddingNode.isMissingNode() || !embeddingNode.isArray() || embeddingNode.size() == 0) {
            log.warn("Пустой/некорректный embedding");
            return ResponseEntity.badRequest().body("Пустой или некорректный embedding.");
        }

// Преобразование embedding (если дальше требуется)
        float[] vector = new float[embeddingNode.size()];
        for (int i = 0; i < embeddingNode.size(); i++) {
            vector[i] = (float) embeddingNode.get(i).asDouble();
        }
        log.info("Embedding получен. Размер={}", vector.length);

        ResponseEntity<List<CompareVectorsResponse>> compareResp = feingClient.compareVectors(vector);
        List<CompareVectorsResponse> resultCompare = (compareResp != null && compareResp.getStatusCode().is2xxSuccessful())
                ? compareResp.getBody()
                : List.of();
        if (resultCompare == null) {
            resultCompare = List.of();
        }
        log.info("Результатов сравнения = {}", resultCompare.size());

        List<Message> messages = new ArrayList<>();
        if (system != null && !system.isBlank()) {
            messages.add(Message.builder().role("system").text(system +
                    "Инструкция:\n" +
                    "1. Сообщение пользователя было переведено в вектор и сравнилось с базой по L2 (евклидову расстоянию).\n" +
                    "2. Подходящие векторы: \n" + resultCompare +
                    "3. Используй поля:\n" +
                    "   - chunkText для основного текста ответа\n" +
                    "   - urlDoc и pageNum — укажи в ответе, если они присутствуют\n" +
                    "4. Построй ответ на вопрос пользователя на основе ближайших векторов.\n" +
                    "5. Если расстояние слишком большое (например, > 0.6) или подходящих векторов нет, используй внешние источники (Wikipedia, Habr).\n" +
                    "\n" +
                    "Формат ответа:\n" +
                    "- Краткий связный текст.\n" +
                    "- Если есть urlDoc/pageNum — добавь их явно.").build());
        }
        messages.add(Message.builder().role("user").text(prompt).build());


        YandexGptRequest request = YandexGptRequest.builder()
                .modelUri("gpt://" + props.getFolderId() + "/yandexgpt-5-lite/latest")
                .completionOptions(CompletionOptions.builder()
                        .stream(false)
                        .temperature(0.1)
                        .maxTokens(1000)
                        .reasoningOptions(ReasoningOptions.builder()
                                .mode(ReasoningMode.DISABLED)
                                .build())
                        .build())
                .messages(messages)
                .build();

        return ResponseEntity.ok(completion(request));
    }

    public String completion(YandexGptRequest request) {
        JsonNode response = client.post()
                .uri("/foundationModels/v1/completion")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp ->
                        resp.bodyToMono(String.class)
                                .map(body -> new RuntimeException("Yandex API error " + resp.statusCode() + ": " + body))
                )
                .bodyToMono(JsonNode.class)
                .block(Duration.ofSeconds(30));
        String response_str = response.path("result")
                .path("alternatives")
                .get(0)
                .path("message")
                .path("text")
                .asText();;

        return  response_str;
    }



}
