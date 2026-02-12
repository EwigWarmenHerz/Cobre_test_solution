package com.taller.cobre.infrastructure.output_adapters.cache_adapter;

import com.taller.cobre.domain.model.client.ClientRouting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@Slf4j
@RequiredArgsConstructor
public class CacheRepository {
    private final ReactiveRedisTemplate<String, ClientRouting> reactiveRedisTemplate;
    private static final String KEY_PREFIX = "client:config:";

    public Mono<ClientRouting> getClientRouting(long clientId) {
        return reactiveRedisTemplate.opsForValue()
            .get(KEY_PREFIX + clientId)
            .cast(ClientRouting.class)
            .doOnNext(config -> log.debug("Cache hit para cliente: {}", clientId))
            .doOnSuccess(clientRouting -> log.info("Get cached client: " + clientRouting));
    }

    public Mono<Void> cacheClient(ClientRouting routing) {
        return reactiveRedisTemplate.opsForValue()
            .set(KEY_PREFIX + routing.id(), routing, Duration.ofHours(1))
            .doOnNext(success -> log.debug("Cache save para cliente {}: {}", routing.id(), success))
            .then();
    }

    public Mono<Void> evictClientConfig(long clientId) {
        return reactiveRedisTemplate.opsForValue()
            .delete(KEY_PREFIX + clientId)
            .then();
    }
}
