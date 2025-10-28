package ru.twentyoneh.embedderservice.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

@Slf4j
public class WebClientLoggingFilters {

    public static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(req -> {
            log.info("--- OUTGOING {} {}", req.method(), req.url());
            req.headers().forEach((k, v) -> log.info("{}: {}", k, String.join(",", v)));
            return Mono.just(req);
        });
    }

    public static ExchangeFilterFunction logResponse() {
        return ExchangeFilterFunction.ofResponseProcessor(res -> {
            log.info("<--- INCOMING status={}", res.statusCode());
            res.headers().asHttpHeaders().forEach((k, v) -> log.info("{}: {}", k, String.join(",", v)));
            return Mono.just(res);
        });
    }
}
